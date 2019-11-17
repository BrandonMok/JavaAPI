package timecard;

import companydata.*;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;
import com.google.gson.*;

/**
* CompanyServices
* @author Brandon Mok
* CS handles pathing and directs functionality of routes
*/
@Path("CompanyServices")
public class CompanyServices {
   
   // GSON 
   GsonBuilder builder = new GsonBuilder();
   Gson gson = builder.create();
   
   // Data Layer
   DataLayer dl = null;
   
   // Produces JSON string - ALL functions return json
   public static final String json = "application/json";
   
   // Business layer - validation
   BusinessLayer bl = new BusinessLayer();
   
   
   /** ---------- COMPANY ---------- */
   
   @Path("company")
   @DELETE
   @Produces(json)
   public Response deleteCompany( 
      @QueryParam("company") String company 
   ){
      try {
         if(bl.notNull(company)){
           if(bl.validateCompany(company)){
                 // 1) Delete all timecards
                 // 2) Delete all employees
                 // 3) Delete all departments
                 // 4) Delete company
                 
                 // Employees
                 List<Employee> employeeList = dl.getAllEmployee(company);
                 if(employeeList.size() > 0){
                     for(Employee emp : employeeList){
                        // Timecards
                        List<Timecard> timecardList = dl.getAllTimecard(emp.getId());
                        if(timecardList.size() > 0){
                           for (Timecard tc : timecardList){
                              dl.deleteTimecard(tc.getId());   // delete Timecard(s)
                           }
                        }
                        dl.deleteEmployee(emp.getId());        // delete Employee(s)
                     }
                 }
                 
                 // Departments
                 List<Department> departmentList = dl.getAllDepartment(company);
                 if(departmentList.size() > 0){
                     for (Department dep : departmentList){
                        dl.deleteDepartment(company, dep.getId());   // delete Department(s)
                     }
                 }
               
                 int rows = dl.deleteCompany(company); // delete Company
                 if(rows > 0){
                     // If rows is > 0, then delete was successful
                     return bl.ok(" Company " + company + "'s information deleted");
                  }
                  else {
                     // Delete didn't return any rows
                     return bl.errorResponse("INTERNAL_SERVER_ERROR", rows + " rows affected");
                  }
            }
            else {   
               return bl.errorResponse("BAD_REQUEST", " Company " + company + " entered is invalid!");
            }
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Company not entered!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
          bl.closeDL(dl);
      }  
   }
 
 
 
   
   /** ---------- DEPARTMENT ---------- */
   
   // localhost:8080/MokBP2/resources/CompanyServices/departments?company={company}
   @Path("departments")
   @GET
   @Produces(json)
   public Response getAllDepartment(
      @QueryParam("company") String company
   ){
      try {
         // CHECK: Company was entered
         if(bl.notNull(company)){
            // CHECK: Entered company is mine!
            if(bl.validateCompany(company)){ 
               dl = new DataLayer(company);
               
               // Get all departments from datalayer
               List<Department> departmentsList = dl.getAllDepartment(company);
               List<String> departments = new ArrayList<String>();
               
               // CHECK: There are departments available
               if(departmentsList.size() > 0){               
                  // Iterate through list, convert each department into a JSON string, add to different list, return that list
                  for(int i = 0; i < departmentsList.size(); i++){
                     departments.add(bl.departmentToJSON(departmentsList.get(i)));
                  }
                  return bl.ok(departments); // return converted list!
               }
               else {
                  // ERROR: There are no departments
                  return bl.errorResponse("NOT_FOUND", " No departments found!");
               }
            }
            else {
               // ERROR: Not my company!
               return bl.errorResponse("BAD_REQUEST", " Company " + company + " entered is invalid!");
            }  
         }
         else {
            // ERROR: Company not entered
            return bl.errorResponse("BAD_REQUEST", " Company not supplied!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());     
      }
      finally{
         bl.closeDL(dl);
      }
   }
   
   // localhost:8080/MokBP2/resources/CompanyServices/department?company={company}&dept_id={dept_id}   
   @Path("department")
   @GET
   @Produces(json)
   public Response getDepartment(
      @QueryParam("company") String company,
      @QueryParam("dept_id") int dept_id
   ) {
      try{
         // CHECK: Company and department ID was passed
         if(bl.notNull(company) && dept_id != 0){
            // CHECK: Entered company is mine!
            if(bl.validateCompany(company)){
               dl = new DataLayer(company);      
               
               Department dep = dl.getDepartment(company, dept_id);
          
               if(bl.notNull(dep)){
                  // Return OK    
                  return bl.ok(bl.departmentToJSON(dep));
                }
                else {
                  // ERROR: Department NOT_FOUND   
                  return bl.errorResponse("NOT_FOUND"," Department not found with ID: " + String.valueOf(dept_id));
                }
            }
            else {
               // ERROR: Company is not mine!
               return bl.errorResponse("BAD_REQUEST", " Company " + company + " entered is invalid!");
            } 
         }
         else {
            // ERROR: Company and/or deparment ID not passed
            return bl.errorResponse("BAD_REQUEST", " Company and/or department ID not supplied!");
         } 
      }
      catch(Exception e){
        return bl.errorResponse("ERROR",e.getMessage());    
      }
      finally{
         bl.closeDL(dl);
      }
   }
    
   @Path("department")
   @POST
   @Produces(json)
   public Response insertDepartment(
      @FormParam("dept_id") int dept_id,
      @FormParam("company") String company,
      @FormParam("dept_name") String dept_name,
      @FormParam("dept_no") String dept_no,
      @FormParam("location") String location
   ){
      try{
         // List of entered fields
         List<Object> fieldsList = new ArrayList<>();
            fieldsList.add(company);
            fieldsList.add(dept_name);
            fieldsList.add(dept_no);
            fieldsList.add(location);
         
         // Validate that entered fields aren't null!
         if(bl.inputFieldsNotNull(fieldsList)){
            if(bl.validateCompany(company)){
              dl = new DataLayer(company);
              
              Department dep = null;
              
              // CHECK: if dept_id was passed in or not
              if(bl.notNull(dept_id)){
                  dep = new Department(dept_id, company, dept_name, dept_no, location);
              }
              else {
                  dep = new Department(company, dept_name, dept_no, location);
              }
              
              Department validatedDep = bl.validateDepartment(dep, company, "POST");
              if(bl.notNull(validatedDep)){
                  // Perform Department Insert
                  // return JSON string version of department
                  validatedDep = dl.insertDepartment(validatedDep);
                  return bl.ok(bl.departmentToJSON(validatedDep));
              }
              else {
                  // ERROR: Department validation failed!
                  return bl.errorResponse("BAD_REQUEST", " Invalid field(s) input!");
              } 
            }
            else {
               // ERROR: Company isn't mine!
               return bl.errorResponse("BAD_REQUEST", " Company " + company + " entered is invalid!");
            }
         }
         else {
            // ERROR: Not all fields entered 
            return bl.errorResponse("BAD_REQUEST", " Invalid field(s) entered!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally{
         bl.closeDL(dl);
      }
   }
   
   @Path("department")
   @PUT
   @Consumes("application/json")
   @Produces("application/json")
   public Response updateDepartment(String json){   
     try{
         // Convert json string into json Object
         JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();     
         Set<String> keys = jsonObject.keySet();
         
         // CHECK: if company and dept_id were at least passed
         if(keys.contains("company") && keys.contains("dept_id")){
              String company = jsonObject.get("company").getAsString();
              String dept_id = jsonObject.get("dept_id").getAsString();
              
              // CHECK: if the entered company is mine
              if(bl.validateCompany(company)){
                  dl = new DataLayer(company); 

                  Department dep = dl.getDepartment(company, Integer.parseInt(dept_id));
                  
                  if(bl.notNull(dep)){                  
                     for(String eachKey : keys){
                        if(!eachKey.equals("company") || !eachKey.equals("dept_id")){
                           switch(eachKey.toLowerCase()){
                              case "dept_name":
                                 dep.setDeptName(jsonObject.get(eachKey).getAsString());
                                 break;
                              case "dept_no":
                                 dep.setDeptNo(jsonObject.get(eachKey).getAsString());
                                 break;
                              case "location":
                                 dep.setLocation(jsonObject.get(eachKey).getAsString());
                                 break;
                           }
                        }
                     } 

                     // Validate updated department object
                     dep = bl.validateDepartment(dep, company, "PUT");
                     
                     // If the passed in department object passed validation it won't be null
                     if(bl.notNull(dep)){
                        // Make sure that the returned Department from the Data Layer update method isn't null
                        if(bl.notNull(dl.updateDepartment(dep))){  
                           return bl.ok(bl.departmentToJSON(dep));    // Successful update!
                        }
                        else {
                           return bl.errorResponse("BAD_REQUEST", " Update failed on provided inputs!");
                        }
                     }
                     else {
                        return bl.errorResponse("BAD_REQUEST", " Invalid field(s) on update!");
                     }
                  }
                  else {
                     return bl.errorResponse("NOT_FOUND", " Department " + dept_id + " not found!");
                  }                
              }
              else {
                  return bl.errorResponse("BAD_REQUEST", " Company " + company + " entered is invalid!");
              }     
         }
         else{
            // ERROR: Company and/or dept_id wasn't passed which are both required to perform an update!
            return bl.errorResponse("BAD_REQUEST", " Company and/or dept_id not entered for update of department!");
         }  
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally{
          bl.closeDL(dl);
      }
   }
   
   @Path("department")
   @DELETE
   @Produces(json)
   public Response deleteDepartment(
      @QueryParam("company") String company,
      @QueryParam("dept_id") int dept_id
   ){
      try{
         // CHECK: company and dept_id were passed
         if(bl.notNull(company) && dept_id != 0){
            if(bl.validateCompany(company)){
               dl = new DataLayer(company);
                
               // GET department first to verify that it exists!
               Department dep = dl.getDepartment(company, dept_id);
               if(bl.notNull(dep)){
                  // 1) Delete timecards
                  // 2) Delete Employees
                  // 3) Finally delete department
               
                  // Employees for this department
                  List<Employee> empList = dl.getAllEmployee(company);
                  if(empList.size() > 0) {
                     for(Employee employee : empList){
                        if(employee.getDeptId() == dep.getId()){  // make sure employee's deptID == dept_id 
                           // For the employee, get their timecards and delete them!
                           List<Timecard> timecardList = dl.getAllTimecard(employee.getId());
                           for(Timecard tcard : timecardList){
                              dl.deleteTimecard(tcard.getId());   // delete Timecard(s)
                           }
                           
                           dl.deleteEmployee(employee.getId());   // delete Employee
                        }
                     }
                  }
                            
                  // Delete Department
                  int rows = dl.deleteDepartment(company, dept_id);  // deleteDepartment
                  if(rows > 0){
                     return bl.ok(" Department " + dept_id + " from " + company + " deleted");
                  }
                  else {
                     // ERROR: Delete didn't return any rows
                     return bl.errorResponse("INTERNAL_SERVER_ERROR", rows + " rows affected");
                  }
               }
               else {
                  // ERROR: Department DOES NOT EXIST
                  return bl.errorResponse("NOT_FOUND", " Department " + dept_id + " from " + company + " doesn't exist!");
               }
            }
            else {
               // ERROR: Company isn't mine
               return bl.errorResponse("BAD_REQUEST", " Company " + company + " entered is invalid!");
            } 
         }
         else {
            // ERROR: Company and/or department ID weren't passed in to perform delete
            return bl.errorResponse("BAD_REQUEST", " Company and/or department ID not supplied!");
         } 
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally{
          bl.closeDL(dl);
      }
   }
   
   
   
   
   /** ---------- EMPLOYEE ---------- */
   
   // localhost:8080/MokBP2/resources/CompanyServices/employees?company={company}
   @Path("employees")
   @GET
   @Produces(json)
   public Response getAllEmployee(
      @QueryParam("company") String company
   ){
      try{
         // CHECK: Company was passed in
         if(bl.notNull(company)){
            if(bl.validateCompany(company)){
               dl = new DataLayer(company);
               
               // Get all departments from datalayer
               List<Employee> employeeList = dl.getAllEmployee(company);
               List<String> employees = new ArrayList<String>();
               
               // CHECK: employeeList contains retrieved employee objects
               if(employeeList.size() > 0){
                  for(int i = 0; i < employeeList.size(); i++){
                     employees.add(bl.employeeToJSON(employeeList.get(i)));
                  }

                  return bl.ok(employees);   // return OK
               }
               else {
                  return bl.errorResponse("NOT_FOUND", " No employees found!");
               }
            }
            else {
               return bl.errorResponse("BAD_REQUEST", " Company " + company + " entered is invalid!");
            } 
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Company not supplied!");
         } 
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage()); 
      }
      finally{
          bl.closeDL(dl);
      }
   }
   
   // localhost:8080/MokBP2/resources/CompanyServices/employee?company={company}&emp_id={emp_id}
   @Path("employee")
   @GET
   @Produces(json)
   public Response getEmployee(
      @QueryParam("company") String company,
      @QueryParam("emp_id") int emp_id 
   ){
      try{
         // CHECK: company and emp_id were passed
         if(bl.notNull(company) && emp_id != 0){
            if(bl.validateCompany(company)){
               dl = new DataLayer(company);
               
               Employee emp = dl.getEmployee(emp_id);
               if(bl.notNull(emp)){
                  // Return OK  
                  return bl.ok(bl.employeeToJSON(emp));
                }
                else {
                  // ERROR: Not found  
                  return bl.errorResponse("NOT_FOUND", " Employee not found with ID: " + String.valueOf(emp_id));
                }       
            }
            else {
               return bl.errorResponse("BAD_REQUEST", " Company "+ company +" entered is invalid!");
            }    
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Company and/or employee ID not supplied!");
         }           
      }
      catch(Exception e){
         return bl.errorResponse("ERROR",e.getMessage());
      }
      finally{
          bl.closeDL(dl);
      }
   }     
 
   @Path("employee")
   @POST
   @Produces(json)
   public Response insertEmployee(
      @FormParam("company") String company,
      @FormParam("emp_name") String emp_name,
      @FormParam("emp_no") String emp_no,
      @FormParam("hire_date") java.sql.Date hire_date,   
      @FormParam("job") String job,
      @FormParam("salary") double salary, 
      @FormParam("dept_id") int dept_id,
      @FormParam("mng_id") int mng_id,
      @FormParam("emp_id") int emp_id
   ){
      try{
         // Add all values into a list, pass it into inputFieldsNotNull, and check if all fields (emp_id not necessary) aren't null
         List<Object> fieldsList = new ArrayList<>();
            fieldsList.add(company);
            fieldsList.add(emp_name);
            fieldsList.add(emp_no);
            fieldsList.add(hire_date);
            fieldsList.add(job);
            fieldsList.add(salary);
            fieldsList.add(dept_id);
            fieldsList.add(mng_id);
         
         // First check that all inputs were entered
         if(bl.inputFieldsNotNull(fieldsList)){
            if(bl.validateCompany(company)){
               dl = new DataLayer(company);
                      
               Employee emp = null; 
               if(bl.notNull(emp_id)){
                  emp = new Employee(emp_id, emp_name, emp_no, hire_date, job, salary, dept_id, mng_id);
               }   
               else {
                  emp = new Employee(emp_name, emp_no, hire_date, job, salary, dept_id, mng_id);
               }
               
               // VALIDATE the employee object
               Employee validatedEmp = bl.validateEmployee(emp, company, "POST");
               if(bl.notNull(validatedEmp)){
                  validatedEmp = dl.insertEmployee(validatedEmp); // insertEmployee
                  return bl.ok(bl.employeeToJSON(validatedEmp));
               }
               else {
                  // return error Response
                  return bl.errorResponse("BAD_REQUEST", " Invalid field(s) input!");
               } 
            }
            else {
               return bl.errorResponse("BAD_REQUEST", " Company "+company+" entered is invalid!");
            }    
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Invalid field(s) entered!");
         }  
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
          bl.closeDL(dl);
      }
   }
   
   @Path("employee")
   @PUT
   @Consumes("application/json")
   @Produces("application/json")
   public Response updateEmployee(String json){
     try{
        // Convert json string into json object
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        Set<String> keys = jsonObject.keySet();
         
        // CHECK: if company and emp_id were passed
        if(keys.contains("company") && keys.contains("emp_id")){
           String company = jsonObject.get("company").getAsString();
           int emp_id = jsonObject.get("emp_id").getAsInt();
           
           // CHECK: if entered company is MINE
           if(bl.validateCompany(company)){
               dl = new DataLayer(company);
               
               Employee employee = dl.getEmployee(emp_id);
               if(bl.notNull(employee)){
                  for(String eachKey : keys){
                     if(!eachKey.equals("company") || !eachKey.equals("emp_id")){
                        switch(eachKey.toLowerCase()){
                           case "emp_name":
                              employee.setEmpName(jsonObject.get(eachKey).getAsString());
                              break;
                           case "emp_no":
                              employee.setEmpNo(jsonObject.get(eachKey).getAsString());
                              break;
                           case "hire_date":
                              java.sql.Date hDate = bl.stringToDate(jsonObject.get(eachKey).getAsString());   
                              employee.setHireDate(hDate);
                              break;
                           case "job":
                              employee.setJob(jsonObject.get(eachKey).getAsString());
                              break;
                           case "salary":
                              employee.setSalary(jsonObject.get(eachKey).getAsDouble());
                              break;
                           case "dept_id":
                              employee.setDeptId(jsonObject.get(eachKey).getAsInt());
                              break;
                           case "mng_id":
                              employee.setMngId(jsonObject.get(eachKey).getAsInt());
                              break;
                        }
                     }
                  }
                  
                  // Validate updated Employee object
                  employee = bl.validateEmployee(employee, company, "PUT");
                  if(bl.notNull(employee)){
                     // Make sure that the returned Employee from the Data Layer update method isn't null
                     if(bl.notNull(dl.updateEmployee(employee))){
                        return bl.ok(bl.employeeToJSON(employee));
                     }
                     else {
                        return bl.errorResponse("BAD_REQUEST", " Update failed on employee!");
                     }
                  }
                  else {
                     return bl.errorResponse("BAD_REQUEST", " Invalid field(s) for inputs!");
                  }
               }
               else {
                  return bl.errorResponse("NOT_FOUND", " Employee " + emp_id + " not found for company " + company);
               }
           }
           else {
               return bl.errorResponse("BAD_REQUEST", " Company "+company+" entered is invalid!");
           }        
        }
        else {
            return bl.errorResponse("BAD_REQUEST", " Invalid and/or missing company and employee ID!");
        }           
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
          bl.closeDL(dl);
      }
    }

   
   @Path("employee")
   @DELETE
   @Produces(json)
   public Response deleteEmployee(
      @QueryParam("company") String company, 
      @QueryParam("emp_id") int emp_id
   ){
      try {
         // CHECK: company and emp_id were passed in
         if(bl.notNull(company) && emp_id != 0){  
            if(bl.validateCompany(company)){
               dl = new DataLayer(company);
               
               // Get employee to make sure it exists
               Employee emp = dl.getEmployee(emp_id);
               
               if(bl.notNull(emp)){
                  // DELETE this employee's timecard!
                  List<Timecard> timecardList = dl.getAllTimecard(emp.getId());
                  if(timecardList.size() > 0){
                     for (Timecard tc : timecardList){
                        // Delete timecards that this employee has!
                        dl.deleteTimecard(tc.getId());
                     }
                  }
               
                  int rows = dl.deleteEmployee(emp_id);  // deleteEmployee
                  if(rows > 0){
                     return bl.ok(" Employee with ID " + emp_id + " from " + company + " deleted!");
                  }
                  else {
                     return bl.errorResponse("INTERNAL_SERVER_ERROR", rows + " rows affected");
                  }
               }
               else {
                  // Employee doesn't exist!
                  return bl.errorResponse("NOT_FOUND", " Employee " + emp_id + " not found!");
               }
            }
            else {
               return bl.errorResponse("BAD_REQUEST", " Company "+company+" entered is invalid!");
            }
         }
         else {
            // ERROR: user didn't enter required company and emp_id
            return bl.errorResponse("BAD_REQUEST", " Company and/or employee ID not entered!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
          bl.closeDL(dl);
      }
   }

   
   
   /** ---------- TIMECARD ---------- */
   
   // localhost:8080/MokBP2/resources/CompanyServices/timecards?company={company}&emp_id={emp_id}
   @Path("timecards")
   @GET
   @Produces(json)
   public Response getAllTimecard(
      @QueryParam("company") String company, 
      @QueryParam("emp_id") int emp_id
   ){
      try{
         // CHECK: Company and emp_id were passed in
         if(bl.notNull(company) && emp_id != 0){
            if(bl.validateCompany(company)){
               dl = new DataLayer(company);
               
               List<Timecard> timecardList = dl.getAllTimecard(emp_id);
               List<String> timecards = new ArrayList<String>();
               
               // CHECK: Timecards were retrieved
               if(bl.notNull(timecardList) && timecardList.size() > 0){
                  for(int i = 0; i < timecardList.size(); i++){
                     timecards.add(bl.timecardToJSON(timecardList.get(i)));
                  }
                  return bl.ok(timecards);   // return OK
               }
               else {
                  return bl.errorResponse("NOT_FOUND", " No timecards found for employee " + emp_id + "!");
               } 
            }
            else{
               return bl.errorResponse("BAD_REQUEST", " Company "+ company +" entered is invalid!");
            }
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Company and/or employee ID not supplied!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally{
          bl.closeDL(dl);
      }
   }
   
   // localhost:8080/MokBP2/resources/CompanyServices/timecard?company={company}&timecard_id={timecard_id}
   @Path("timecard")
   @GET
   @Produces(json)
   public Response getTimecard(
      @QueryParam("company") String company,
      @QueryParam("timecard_id") int timecard_id
   ){
      try{
         // CHECK: Company and timecard_id were passed in
         if(bl.notNull(company) && timecard_id != 0){
            if(bl.validateCompany(company)){
               dl = new DataLayer(company);
               
               // Timecard object
               Timecard tc = dl.getTimecard(timecard_id);
               
               if(bl.notNull(tc)){
                  return bl.ok(bl.timecardToJSON(tc));  
               }
               else {
                  return bl.errorResponse("NOT_FOUND", " Timecard " + timecard_id + " not found!");
               } 
            }
            else{
               return bl.errorResponse("BAD_REQUEST", " Company "+ company +" entered is invalid!");
            }
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Company and/or timecard ID not supplied!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally{
         bl.closeDL(dl);
      }
   }
   
   @Path("timecard")
   @POST
   @Produces(json)
   public Response insertTimecard(
      @FormParam("company") String company,
      @FormParam("emp_id") int emp_id,
      @FormParam("start_time") java.sql.Timestamp start_time,
      @FormParam("end_time") java.sql.Timestamp end_time,
      @FormParam("timecard_id") int timecard_id
   ){
      try {
         // List to hold all user entered fields EXCEPT for timecard_id as it's not necessary
         List<Object> fieldsList = new ArrayList<>();
            fieldsList.add(company);
            fieldsList.add(emp_id);
            fieldsList.add(start_time);
            fieldsList.add(end_time);
      
         // CHECK: input fields were all entered (timecard_id not necessary)
         if(bl.inputFieldsNotNull(fieldsList)){
            if(bl.validateCompany(company)){
               dl = new DataLayer(company);
               
               Timecard tc = null;
               if(bl.notNull(timecard_id)){
                  tc = new Timecard(timecard_id, start_time, end_time, emp_id);
               }
               else {
                  tc = new Timecard(start_time, end_time, emp_id);
               }
               
               Timecard validTimecard = bl.validateTimecard(tc, company, "POST");                
               if(bl.notNull(validTimecard)){
                  validTimecard = dl.insertTimecard(validTimecard);  // insertTimecards
                  return bl.ok(bl.timecardToJSON(validTimecard));
               }
               else {
                  // return error Response
                  return bl.errorResponse("BAD_REQUEST", " Invalid field(s) input! Make sure employee exists and times are on a M-F basis between normal working hours!");
               } 
            }
            else {
               return bl.errorResponse("NOT_FOUND", " Company " + company + " entered is invalid!");
            }
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Company and/or timecard not supplied!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
          bl.closeDL(dl);
      }
   }

   @Path("timecard")
   @PUT
   @Consumes("application/json")
   @Produces("application/json")
   public Response updateTimecard(String json){
      try {
         // Convert json string into json object
         JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
         Set<String> keys = jsonObject.keySet();
         
         // CHECK: if company and timecard_id were at least passed
         if(keys.contains("company") && keys.contains("timecard_id")){
            String company = jsonObject.get("company").getAsString();
            int timecard_id = jsonObject.get("timecard_id").getAsInt();
            boolean empIDChanged = false;
            
            // CHECK: company entered is mine!
            if(bl.validateCompany(company)){
               dl = new DataLayer(company);
               
               // Get the timecard for the entered ID
               Timecard tc = dl.getTimecard(timecard_id);
               if(bl.notNull(tc)){
                  DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                  for(String eachKey : keys){
                     if(!eachKey.equals("company") || !eachKey.equals("timecard_id")){
                        switch(eachKey.toLowerCase()){
                           case "start_time":
                              Timestamp start_time = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonObject.get(eachKey).getAsString()).getTime());    
                              tc.setStartTime(start_time);
                              break;
                           case "end_time":
                              Timestamp end_time = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonObject.get(eachKey).getAsString()).getTime());    
                              tc.setEndTime(end_time);
                              break;
                           case "emp_id":
                              int startingID = tc.getId();                       // starting ID before update
                              int newEmpID = jsonObject.get(eachKey).getAsInt(); // newly entered ID
      //                         if(startingID != newEmpID){
//                                  empIDChanged = true; // set flag to know
//                               }
                              tc.setEmpId(newEmpID);
                              break;
                        }
                     }  
                  }
                  
                  // validate updated Timecard object
                  tc = bl.validateTimecard(tc, company, "PUT");
                  if(bl.notNull(tc)){
                     if(bl.notNull(dl.updateTimecard(tc))){
//                         if(empIDChanged){
//                            // IF emp_id was changed on a timecard, have to cha
//                         }
                     
                     
                        return bl.ok(bl.timecardToJSON(tc));
                     }
                     else {
                        return bl.errorResponse("BAD_REQUEST", " Update failed on timecard!");
                     }
                  }
                  else {
                     return bl.errorResponse("BAD_REQUEST"," Invalid field(s) and/or missing company and timecard_id");
                  }        
               }
               else {
                  return bl.errorResponse("NOT_FOUND", " Timecard " + timecard_id + " not found!");
               }  
            }
            else {
               return bl.errorResponse("BAD_REQUEST", " Company " + company + " entered is invalid!");
            } 
         }
         else{
            // ERROR: Company and/or timecard_id not entered
            return bl.errorResponse("BAD_REQUEST", " Company and/or timecard_id not entered!");
         }         
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
         bl.closeDL(dl);
      }
   }


   @Path("timecard")
   @DELETE
   @Produces(json)
   public Response deleteTimecard( 
      @QueryParam("company") String company,
      @QueryParam("timecard_id") int timecard_id
   ){
      try {
         // CHECK: company and timecard_id were passed in
         if(bl.notNull(company) && timecard_id != 0){
            if(bl.validateCompany(company)){
               dl = new DataLayer(company);
               
               Timecard tc = dl.getTimecard(timecard_id);
               if(bl.notNull(tc)){
                  int rows = dl.deleteTimecard(timecard_id);
                  if(rows > 0){
                     return bl.ok(" Timecard " + timecard_id + " deleted");
                  }
                  else {
                     return bl.errorResponse("INTERNAL_SERVER_ERROR", "Timecard " + timecard_id + " failed to delete!");
                  }
               }
               else{
                  return bl.errorResponse("NOT_FOUND", " Timecard " + timecard_id + " cannot be deleted as it's found!");
               }
            }
            else {
               return bl.errorResponse("BAD_REQUEST", " Company " + company + " entered is invalid!");
            }
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Company and/or timecard ID not supplied!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
         bl.closeDL(dl);
      }
   }
}// class