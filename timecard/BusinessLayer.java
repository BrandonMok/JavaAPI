package timecard;
import companydata.*;

import com.google.gson.*;
import java.util.*;
import java.net.*;
import org.apache.commons.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.sql.Timestamp;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
import java.text.*;




public class BusinessLayer {

   // GSON to JSON builders
   GsonBuilder builder;
   Gson gson;
   
   // Data Layer
   DataLayer dl = null;

   /**
   *  Constructor
   */
   public BusinessLayer(){
      builder = new GsonBuilder();  // intialize GsonBuilder
      gson = builder.create();      // Create Gson object
   }
   
   
   
   
   /** ---------- OTHER ---------- */
   /**
   * ok
   * @param List<String>
   * @return Response
   * Returns an OK status response accepting a list
   */
   public Response ok(List<String> list){
      return Response.ok("{\"success\":" + list + "}", MediaType.APPLICATION_JSON).build();
   }
   /**
   * ok
   * @param String 
   * @return Response  
   * Returns an OK status response accepting a String
   */
   public Response ok(String message){
      return Response.ok("{\"success\":" + message  + "}").build();
   }
   
   /**
   * errorResponse
   * @param String,String
   * @return Response
   * Returns an error Response based on a given type of error
   */ 
   public Response errorResponse(String type, String message){
      String errorMsg = "{\"error\":" + message + "}";
   
      switch(type.toUpperCase()){
         case "NOT_FOUND":
            return Response.status(Response.Status.NOT_FOUND).entity(errorMsg).build();
         case "BAD_REQUEST":
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
         case "CONFLICT":
            return Response.status(Response.Status.CONFLICT).entity(errorMsg).build();
         case "INTERNAL_SERVER_ERROR":
         case "ERROR":
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
      }  
      // Need a default return, but won't get here as the errors passed in are from me
      return null;
   }
   
   /**
   * notNull
   * @param String
   * @return boolean
   * Reusable function to test if a given value isn't null (T = not null, F = null)
   */
   public boolean notNull(String value){
      boolean notNull = false;
      if(value != null){
         notNull = true;
      }
      return notNull;
   }
   
   /**
   * uniquePerCompany
   * @param String, String
   * @return String
   * Reusable function to make sure specific fields are company specific (i.e. dept_no, emp_id, ...)
   */
   public String uniquePerCompany(String str, String company){
      String unique = "";
      if(company.equals("bxm5989")){
         if(!str.contains(company)){
            unique = company + str;
         }
         else {
            unique = str;
         }
      }
      else {
         unique = "";
      }
   
     //  if(!str.contains(company)){
//          unique = company + str;
//       }
//       else {
//          unique = str;
//       }
      return unique;
   }

   /**
    * validateDate
    * @param Date
    * @return boolean
    * Validates a date
    */
   public boolean validateDate(String date){
      boolean valid = false;
      try{
         /**
         * Calendar class to verify dates
         * It cannot be Saturday or Sunday
         * Date equal to the current date or earlier (e.g. current date or in the past)
         */   
         Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
         
         // Calendar - determine the day is allowed
         Calendar calendar = Calendar.getInstance(); 
         calendar.setTime(parsedDate);
         
         // Calendar - Current Calendar Date
         Calendar currentCal = Calendar.getInstance();
         Date currentDate = currentCal.getTime();  // Date object for current date
         
         // # day of the week
         int day = calendar.get(Calendar.DAY_OF_WEEK);
         
         // Validate
         if(day >= 2 || day <= 6 && parsedDate.compareTo(currentDate) > 0){
            // Valid day of the week
            valid = true;
         }
         else {
            // Not valid: day of week was Saturday/Sundary and/or enteredDate is in the future
            return valid;
         }              
      }
      catch(ParseException pe){
         // Error with parsing format - wrong entered format
         return false;
      }
      return valid;
   }
  
  
  
  
   /** ---------- DEPARTMENT  ---------- */ 
   
   /**
   * departmentToJSON
   * @param Department
   * @return String
   * Converts Department object to JSON
   */
   public String departmentToJSON(Department department){
      return gson.toJson(department);
   }
   
   /**
   * jsonToDepartment
   * @param String
   * @return Department
   * Converts JSON to Department object
   */
   public Department jsonToDepartment(String department){
      return gson.fromJson(department, Department.class);
   }
   
   /**
   * validateDeptNo
   * @param String, String
   * @return boolean
   * Validates that a department Number doesn't already exist, so unique
   */
   public boolean validateDeptNo(String company, String dept_no){
      boolean valid = true;
      try{
         dl = new DataLayer(company);
         
         // Get all departments for the company
         List<Department> departmentsList = dl.getAllDepartment(company); 
         
         // Cycle through all to find if dept_no (company+dept_no) exists already as it needs to be unique
         for (Department dep : departmentsList){
            if(dep.getDeptNo() == dept_no){
               valid = false;
               break;
            }
         }
         
         return valid;    
      }
      catch(Exception e){
         System.out.println(e);
      }
      finally{
         dl.close();
      }
      return valid;
   }
   
   /**
   * validateDeptID
   * @param String, int
   * @return boolean
   * Validates a department's ID to see if it already exists
   */
   public boolean validateDeptID(String company, int dept_id){
      boolean valid = false;
      try{
         dl = new DataLayer(company);
         
         // Get a specific department based on company and ID to verify if it exists already or not
         Department dep = dl.getDepartment(company, dept_id);
              
         // Valid only if a department wasn't returned!
         // Doesn't exist yet
         if(dep == null){
            valid = true;
         }
         return valid;
      }
       catch(Exception e){
         System.out.println(e);
      }
      finally{
         dl.close();
      }
      return valid;
   }
   
   
//    public boolean validateDepartment(String department, Map<String, String> fields){
//       boolean valid = false;
//       try{
//          // Create Department object from String
//          // ACCEPTS a STRING to avoid having to make two functions to accept both
//          Department dep = this.jsonToDepartment(department);       
//          dl = new DataLayer(dep.getCompany());  // datalayer
//          
//          for (Map.Entry<String,String> entry : fields.entrySet()){
//             switch(entry.getKey()){
//                case "dept_id":
//                   // Validate that dept_id is unique
//                   
//                   break;
//                case "company":
//                   break;
//                case "dept_name":
//                   break;
//                case "dept_no":
//                   break;
//                case "location":
//                   break;
//             }
//          } 
//          
//          
//       }
//       catch(Exception e){
//          System.out.println(e);
//       }
//       finally{
//          dl.close();
//       }
//    }
   
 
   
   /** ---------- EMPLOYEE  ---------- */ 
   
   /**
   * employeeToJSON
   * @param Employee
   * @return String
   * Converts Employee object to JSON
   */
   public String employeeToJSON(Employee employee){
      return gson.toJson(employee);
   }
   
   /**
   * jsonToEmployee
   * @param String
   * @return Employee
   * Converts JSON to Employee object
   */
   public Employee jsonToEmployee(String employee){
      return gson.fromJson(employee, Employee.class);
   }
   
   
   /**
   public boolean validateEmployee(Employee emp, String company, Map<String, String> fields){
      boolean valid = false;
      try{      
         dl = new DataLayer(company);  // datalayer
         
         for (Map.Entry<String,String> entry : fields.entrySet()){
            switch(entry.getKey()){
               case "emp_name":     
                  break;
               case "emp_no":
                  break;
               case "hire_date":
                  break;
               case "job":
                  break;
               case "salary":
                  break;
               case "dept_id":
                  break;
               case "mng_id":
                  break;
            }
         } 
         
         
      }
      catch(Exception e){
         System.out.println(e);
      }
      finally{
         dl.close();
      }
   }
   */
   
   
   
   /** ---------- TIMECARD  ---------- */ 
   
   /**
   * timecardToJSON
   * @param Timecard
   * @return String
   * Converts Timecard object to JSON
   */
   public String timecardToJSON(Timecard timecard){
      return gson.toJson(timecard);
   }
   
   /**
   * jsonToTimecard
   * @param String
   * @return Timecard
   * Converts JSON to Timecard object
   */
   public Timecard jsonToTimecard(String timecard){
      return gson.fromJson(timecard, Timecard.class);
   }  
}// end business layer