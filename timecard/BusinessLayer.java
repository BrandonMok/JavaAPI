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
   
   // Company - must be bxm5989
   public static final String MYCOMPANY = "bxm5989";

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
   public boolean notNull(Object obj){
      boolean notNull = false;
      if(obj != null){
         notNull = true;
      }
      return notNull;
   }
   
   /**
   * validateCompany
   * @param String
   * @return boolean
   * Validates that the requested company info is MINE
   */
   public boolean validateCompany(String company){
      boolean valid = false;
      if(company.equals(MYCOMPANY)){
         valid = true;
      }
      return valid;
   }
   
   /**
   * uniquePerCompany
   * @param String, String
   * @return String
   * Reusable function to make sure specific fields are company specific (i.e. dept_no, emp_id, ...)
   */
   public String uniquePerCompany(String str, String company){
      String unique = "";
      if(this.validateCompany(company)){
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
      return unique;
   }

   /**
    * validateDate
    * @param Date
    * @return boolean
    * Validates a date (employee hire_date)
    */
   public boolean validateDate(Date date){
      try{
          boolean valid = false;
            
   		 DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
   		 String dateStr = df.format(date);
   		 Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
   		 
   		 // Calendar - Calendar for passed in date!
   		 Calendar calendar = Calendar.getInstance(); 
   		 calendar.setTime(parsedDate);
   		 
   		 // Calendar - Current Calendar Date
   		 Calendar currentCal = Calendar.getInstance();
   		 Date currentDate = currentCal.getTime();  // Date OBJ for current date
   		 
         // # day of the week
         int day = calendar.get(Calendar.DAY_OF_WEEK);                     // #'ed day of the week (1-7)
         int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);             // #'ed day of the month   (1-(29-31))
         int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);   // maximum # of days in the month
        
         // validate
         if((day >= 2 && day <= 6) && 
            (dayOfMonth > 0 && dayOfMonth <= maxDays) && 
            (parsedDate.equals(currentDate) || parsedDate.before(currentDate))
         ){
            // Is valid!
            valid = true;  
         }
   
          return valid;            
      }
      catch(ParseException pe){
         // Error with parsing format - wrong entered format
         return false;
      }
   }
   
   /**
   * validString
   * @param String
   * @return boolean
   * Reusable function to validate a string that shouldn't have any numbers
   */
   public boolean validString(String str){
      boolean valid = false;
      // CHECK: String doesn't numbers!
      if(!str.matches(".*\\d.*")){
         valid = true;
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
   * validateEmployee
   * @param Employee, String, String
   * @return Employee
   * Validates an employee object, returns an Employee Object as if there's any adjustments/changes will need to use changed object
   */
   public Employee validateEmployee(Employee emp, String company, String action){
      try{   
         // Same validation for INSERT and UPDATE (UPDATE has an extra check if the record exists in the first place)
         // Validate company is first of all my company
         if(this.validateCompany(company)){
             dl = new DataLayer(company);  // datalayer 
             
             // CHECK: if employee already exists
             // For PUT, want it to exist so if notNull returns false (is null)  return false
             // For POST, don't want it to exist so if notNUll returns true (object returned) return false
             Employee employee = dl.getEmployee(emp.getId());
             if(!this.notNull(employee) && action.equals("PUT")){
               return null;
             }
             else if (this.notNull(employee) && action.equals("POST")){
               return null;
             }
             
             
             // Dept_ID check - must be an existing department
             Department department = dl.getDepartment(company, emp.getDeptId());
             if(!this.notNull(this.departmentToJSON(department))){ 
               return null; 
             }
             
             /**
             * mng_id
             * Must be a record ID of an EXISTING employee!
             * Set to 0 if the first employee OR to another employee that doesn't have a manager
             */
             // GET EMPLOYEE OBJ with an emp_id == mng_id
             // get all employees match, find if one of their mng_id is the emp_id of an existing employee
             // if not will just set to 0
             
             boolean existingEmp = false;
             List<Employee> employeeList = dl.getAllEmployee(company);
             for (Employee employ : employeeList){
               // Verify that the entered mng_id is the emp_id of an existing employee
               if(employ.getId() == emp.getMngId()){
                  existingEmp = true;
                  break;
               }
             } 
             // if existingEmp was never found, set to 0
             if(!existingEmp){
               emp.setMngId(0);
             }
             

             //emp_no
             //Use uniquePerCompany() which handles making emp_no unique
             String emp_no = this.uniquePerCompany(emp.getEmpNo(), company);
             if(!this.notNull(emp_no)){ 
               return null; 
             }
             
             // emp_name + job
             if(!validString(emp.getEmpName()) || !validString(emp.getJob())){
               return null;
             }
             
             // hire_date
             if(!validateDate(emp.getHireDate())){
               return null;
             }
             
             // salary
//              try{
//              
//              }
//              catch(){
//              
//              } 
         }
         // lastly, return employee object whether null or not
         // Null => something invalid
         // Not null => everything is fine
         return emp;
      }
      catch(Exception e){
         System.out.println(e);
      }
      finally{
         dl.close();
      }
      return emp;
   }
  
   
   
   
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