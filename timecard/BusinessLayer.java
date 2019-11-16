package timecard;
import companydata.*;

import com.google.gson.*;
import java.util.*;
import java.net.*;
import org.apache.commons.io.*;
import java.nio.charset.StandardCharsets;
// import java.util.List;
// import java.util.Calendar;
// import java.util.Date;
import java.sql.Timestamp;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
import java.text.*;

/**
* BusinessLayer
* @author Brandon Mok
* BL handles validation of user input
*/
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
   
   /*
    * inputFieldsNotNull
    * @param List<Object>
    * @return boolean
    * Validates that a list of input fields from user aren't null
    * Used in POST cases where all fields are required to be entered!
    */
   public boolean inputFieldsNotNull(List<Object> fieldsList){
      boolean valid = true;
      
      for (Object obj : fieldsList){
         if((obj instanceof Integer)){
            if((int)obj == 0){
               valid = false;
               break;
            }
         }
         else if(obj instanceof Double){
            if((Double)obj == 0.0){
               valid = false; 
               break;
            }
         }
         else if(obj instanceof String){
            if(!this.notNull((String)obj)){
               valid = false;
               break;
            }
         }
         else if((obj instanceof java.sql.Date) || (obj instanceof java.sql.Timestamp)){
            if(obj == null){
               valid = false;
               break;
            }
         }
      }
      
      return valid;
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
    * closeDL
    * @param DataLayer
    * Closes the datalayer object passed in if it existed
    */
   public void closeDL(DataLayer dl){
      if(this.notNull(dl)){
         dl.close();
      }
   }
   
   /**
    * stringToDate
    * @param String
    * @return Date
    */
   public java.sql.Date stringToDate(String date){
      return java.sql.Date.valueOf(date);
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
   * validateTimestamp
   * @param Timestamp
   * @return boolean
   * Validates a given timestamp
   */
   public boolean validateTimestamp(int employeeID, Timestamp startTime, Timestamp endTime){
      try{
         // Create dateformat template -> format the timestamp to a string -> reformat back to timestamp by parsing 
         // If anything goes wrong in between, then timestamp isn't in right format
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         Timestamp start = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(df.format(startTime)).getTime());  
         Timestamp end = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(df.format(endTime)).getTime());  
         
         // Start_time must be valid date and time equal to current day or 1 week ago from current date
         Date startDate = new Date(start.getTime());
         Date endDate = new Date(end.getTime());
         
         Calendar startCal = Calendar.getInstance();
         Calendar endCal = Calendar.getInstance();
         startCal.setTime(startDate);
         endCal.setTime(endDate);
         
         
         // Validate startDate & endDate - must be on a M-F basis
         if(!this.validateDate(startDate) || !this.validateDate(endDate)){
            return false;
         }
         
         // start_time must be a valid date and time equal to the current date or up to 1 week ago from the current date
         SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
         Calendar c = Calendar.getInstance();
         c.set(Calendar.DATE, c.get(Calendar.DATE)-7);   // check timestamp from a week or go
         Date pastDate = c.getTime();
         
         // CURRENT
         Calendar today = Calendar.getInstance();
         Date todayDate = today.getTime();   
         
         // start cannot be the after today AND cannot be before at most a week ago!  ( [1 week ago - today] )
         if(start.after(todayDate) || start.before(pastDate)){  
            return false;
         }
         
         // end_time needs to be on the same day as start_Time and at least 1 hour greater than start_time
         // IF it made it past the this.validateDate() then format is fine, so proceed
         // if NOT on the same day OR endDate is before the starting date OR startDate is greater than endDate an hour after startDate
         if( (startCal.get(Calendar.DAY_OF_MONTH) != endCal.get(Calendar.DAY_OF_MONTH)) || 
              (startCal.get(Calendar.YEAR) != endCal.get(Calendar.YEAR)) ||
              (startCal.get(Calendar.DATE) != endCal.get(Calendar.DATE)) || 
              endDate.before(startDate) || startDate.getHours() > endDate.getHours() + 1){
            return false;
         }
    
         // Time must be within 06:00:00 - 18:00:00
         // If not return false, don't continue
         if( (start.getHours() < 6 || start.getHours() > 18) ||   
             (end.getHours() < 6 || end.getHours() > 18) ||
             (start.getHours() == end.getHours())){ 
             return false;   
         }
         
          
         // Start_time cannot be on the same day as any other other start_time for that employee
         List<Timecard> timeList = dl.getAllTimecard(employeeID);
         for (Timecard tCard : timeList){
            if(tCard.getStartTime() == start){
               return false;  // Can't have another timecard with the same starttime 
            }
         }
         
        return true;   // if false wasn't returned, true will be returned
      }
      catch(ParseException pe){
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
   * validateDepartment
   * @param Department, String, String
   * @return Department
   * Validates a Deparment object 
   */
   public Department validateDepartment(Department dep, String company, String action){
      try{
         if(this.validateCompany(company)){
            dl = new DataLayer(company);
            
            Department department = dl.getDepartment(company, dep.getId());  
            List<Department> depList = dl.getAllDepartment(company);
            if(action.equals("PUT")){
               if(!this.notNull(department)){
                  // PUT: don't want the department to be null when trying to update it
                  return null;
               }        
            }
            else if(action.equals("POST")){
               if(this.notNull(department)){
                  // POST: don't want obj to exist already
                  return null;
               }
            }

            // Validate - dept_no must be unique among from existing ones
            String dep_no = this.uniquePerCompany(dep.getDeptNo(), company);
         
            // FIND if there's an existing department w/the same dep_no
            List<Department> dList = dl.getAllDepartment(company);
            
            // Cycle through all departments
            for (Department dept : dList){
               // if department # == the uniquely created/modified dep_no
               if(dept.getDeptNo() == dep_no){
                  // CHECK: which action
                  // POST: Return null bc that means the dep_no is already used
                  // PUT: Department object already exists, so make sure that the found department is the same as one being modified (one in function parameters)
                  if(action.equals("POST")){
                     return null; // there's a department that already exists with that dep_no
                  }
                  else if(action.equals("PUT")){
                     // If this department object isn't the same as one passed in function, BUT has the SAME dep_no
                     // then something went wrong
                     if(dep.getId() != dept.getId()){
                        return null;
                     }
                  }
               }
            }
            
            // If there wasn't a department found with the uniquely created/modifed dep_no, 
            // and the unique dep_no wasn't changed from validation compared to one entered by user
            // set the object's dep_no to the uniquePerCompany()
            if(dep_no != dep.getDeptNo()){
               dep.setDeptNo(dep_no);
            }
            
            return dep;
         }

         return null;
      }
      catch(Exception e){
         System.out.println(e);
      }
      finally{
         dl.close();
      }
      return dep;
   }
   
 
   
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
             List<Employee> employeeList = dl.getAllEmployee(company);
             if(employeeList.size() == 0){
               // No employees - set mng_id to 0
               emp.setMngId(0); 
             }
             else {
                boolean existingEmp = false;
                Employee tempEmployee = null;
             
                for (Employee employ : employeeList){
                  // Verify that the entered mng_id is the emp_id of an existing employee
                  if(employ.getId() == emp.getMngId()){
                     existingEmp = true;
                     break;
                  }
                  // Also watchout at the same time if an employee who doesn't have a manager, save that employee for next if
                  // Prevents having to do two iterations over same list if first condition didn't find anything
                  if(employ.getMngId() == 0){
                     tempEmployee = employ;
                  }
                } 
                
                // Employee who's mng_id entered isn't an existing employee
                // Set to an existing manager
                if(!existingEmp){
                  // In case of a PUT, if mng_id doesn't match an existing employee's emp_id (employee DNE), then error out
                  if(action.equals("PUT")){
                     return null;
                  }
                
                  // set employee's mng_id to an employee who doesn't have a manager
                  emp.setMngId(tempEmployee.getId());
                }
             }
             

             //emp_no
             //Use uniquePerCompany() which handles making emp_no unique
             String emp_no = this.uniquePerCompany(emp.getEmpNo(), company);
             for (Employee emply : employeeList){
               if(emply.getEmpNo() == emp_no){
                  // CHECK: which action
                  // POST: Return null bc there's already an employee with the same emp_no
                  // PUT: Employee object already exists, so make sure that the found employee is the same as one being modified (by function parameters)
                  if(action.equals("POST")){
                     return null;
                  }
                  else if (action.equals("PUT")){
                     // if this employee object isn't the same as the one passed in function, BUT has the same emp_no
                     // then something wrong
                     if(emp.getId() != emply.getId()){
                        return null;
                     }
                  }
               }
             }
                        
             // If there wasn't an employee found with the uniquely created/modifed emp_no, 
             // and the unique emp_no wasn't changed from validation compared to one entered by user
             // set the object's emp_no to the uniquePerCompany()
             if(emp_no != emp.getEmpNo()){
               emp.setEmpNo(emp_no);
             }

             // if emp_name and job are both valid strings w/o numbers
             if(!validString(emp.getEmpName()) || !validString(emp.getJob())){
               return null;
             }
             
             // hire_date
             if(!validateDate(emp.getHireDate())){
               return null;
             }
                          
            
            // lastly, return employee object whether null or not
            // Null => something invalid
            // Not null => everything is fine
            return emp;
         }
         
         return null;
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
   
   /**
   * validateTimecard
   * @param Timecard, String, String
   * @return Timecard
   * Validates that a timecard is valid before proceeding to a DL action
   */
   public Timecard validateTimecard(Timecard tc, String company, String action){
      try{
         if(this.validateCompany(company)){
            dl = new DataLayer(company);
            
            // emp_id most be an existing employee
            Employee emp = dl.getEmployee(tc.getEmpId());
            if(!this.notNull(emp)){
               return null;
            }
            
            // Validate Timecard for PUT and POST
            Timecard timecard = dl.getTimecard(tc.getId());
            if(action.equals("PUT")){
               // On PUT, timecard needs to exist
               // if timecard is null, then return null;
               if(!this.notNull(timecard)){
                  return null;
               }
            }
            else if (action.equals("POST")){
               // On POST, don't want timecard to already exist
               // if timecard exists, then return null;
               if(this.notNull(timecard)){
                  return null;
               }
            }   

            // Validate timestamps
            Timestamp startTime = tc.getStartTime();
            Timestamp endTime = tc.getEndTime();
            
            // If startTime and endTime didn't pass timestamp validation
            if(!this.validateTimestamp(tc.getEmpId(), startTime, endTime)){
               return null;
            }

            return tc;
         }
         else {
            // Not my company
            return null;
         }
      }
      catch(Exception e){
         System.out.println(e);
      }
      finally{
         dl.close();
      }
 
      return null;
   }


}// end business layer