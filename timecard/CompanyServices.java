package timecard;

import companydata.*;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
import java.util.*;
import com.google.gson.*;

/**
* CompanyServices
* @author Brandon Mok
* CS handles pathing and directs functionality of routes
*/
@Path("CompanyServices")
public class CompanyServices {

   @Context
   UriInfo uriInfo;
   
   GsonBuilder builder = new GsonBuilder();
   Gson gson = builder.create();
   
   // Data Layer
   DataLayer dl = null;
   
   // Produces JSON string - ALL functions return json
   public static final String json = "application/json";
   
   // Business layer - validation
   BusinessLayer bl = new BusinessLayer();
   
   /** ---------- COMPANY ---------- */
/**   
   @Path("company")
   @DELETE
   @Produces(json)
   public Response deleteCompany( @QueryParam("company") String company ){
      try {
        if(bl.validateCompany(company)){
            // Get all timecards -> delete (FK on emp_id) ( deleteTimecard(int timecard_id) )
            // Get all employee -> delete (FK on dept_id) ( deleteEmployee(int emp_id) )
            // Get all departments -> delete (NO FK) ( deleteDepartment(String company, int dept_id) )
        
            // Do DELETE
            // int rows = dl.deleteCompany(company);
            
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
            return bl.errorResponse("BAD_REQUEST", " company isn't valid");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
         dl.close();
      }  
   }
 */
 
 
 
   
   /** ---------- DEPARTMENT ---------- */
   
   // localhost:8080/MokBP2/resources/CompanyServices/departments?company={company}
   @Path("departments")
   @GET
   @Produces(json)
   public Response getAllDepartment(@QueryParam("company") String company){
      try {
         if(bl.validateCompany(company)){
            dl = new DataLayer(company);
            
            // Get all departments from datalayer
            List<Department> departmentsList = dl.getAllDepartment(company);
            List<String> departments = new ArrayList<String>();
            
            for(int i = 0; i < departmentsList.size(); i++){
               departments.add(bl.departmentToJSON(departmentsList.get(i)));
            }
            
            // return OK
            return bl.ok(departments);
         }
          else {
            return bl.errorResponse("BAD_REQUEST", " Cannot get all departments for company " + company + "!");
         }  
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());     
      }
      finally{
         dl.close();
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
            return bl.errorResponse("BAD_REQUEST", " Cannot get department " + dept_id + " for company " + company + "!");
         }  
      }
      catch(Exception e){
        return bl.errorResponse("ERROR",e.getMessage());    
      }
      finally{
         dl.close();
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
               // return error Response
               return bl.errorResponse("BAD_REQUEST", " Invalid field(s) input!");
           } 
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Cannot insert department " + dept_id + " for company " + company + "!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally{
         dl.close();
      }
   }
   

   @Path("department")
   @PUT
   @Consumes("application/json")
   @Produces("application/json")
   public Response updateDepartment(String json){   
     try{
         JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();     
         Set<String> keys = jsonObject.keySet();
         
         // CHECK: if company and dept_id were at least passed
         if(keys.contains("company") && keys.contains("dept_id")){
              // String company = jsonObject.get("company").toString().trim();
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
                  return bl.errorResponse("BAD_REQUEST", " Cannot update department " + dept_id + " for company " + company + "!");
              }     
         }
         else{
            // User input doesn't have company or dept_id - don't proceed!
            return bl.errorResponse("BAD_REQUEST", " Company and/or dept_id not entered for update of department!");
         }  
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally{
         if(bl.notNull(dl)){
            dl.close();
         }
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
         if(bl.validateCompany(company)){
            dl = new DataLayer(company);
             
            // GET department first to verify that it exists!
            Department dep = dl.getDepartment(company, dept_id);
            if(bl.notNull(dep)){
               int rows = dl.deleteDepartment(company, dept_id);
               if(rows > 0){
                  // If rows is > 0, then delete was successful
                  return bl.ok(" Department " + dept_id + " from " + company + " deleted");
               }
               else {
                  // Delete didn't return any rows
                  return bl.errorResponse("INTERNAL_SERVER_ERROR", rows + " rows affected");
               }
            }
            else {
               // Department DOES NOT EXIST
               return bl.errorResponse("NOT_FOUND", " Department " + dept_id + " from " + company + " doesn't exist!");
            }
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Cannot delete department " + dept_id + " for company " + company + "!");
         }  
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally{
         dl.close();
      }
   }
   
   
   
   
   /** ---------- EMPLOYEE ---------- */
   
   // localhost:8080/MokBP2/resources/CompanyServices/employees?company={company}
   @Path("employees")
   @GET
   @Produces(json)
   public Response getAllEmployee(@QueryParam("company") String company){
      try{
         if(bl.validateCompany(company)){
            dl = new DataLayer(company);
            
            // Get all departments from datalayer
            List<Employee> employeeList = dl.getAllEmployee(company);
            List<String> employees = new ArrayList<String>();
            
            for(int i = 0; i < employeeList.size(); i++){
               employees.add(bl.employeeToJSON(employeeList.get(i)));
            }
            
            // Return OK 
            return bl.ok(employees);
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Cannot get all employees for company " + company + "!");
         }  
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage()); 
      }
      finally{
         dl.close();
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
            return bl.errorResponse("BAD_REQUEST", " Employee " + emp_id + " doesn't belong to company " + company + "!");
         }         
      }
      catch(Exception e){
         return bl.errorResponse("ERROR",e.getMessage());
      }
      finally{
         dl.close();
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
               // perform DL insert
               // return JSON string of newly inserted employee
               validatedEmp = dl.insertEmployee(validatedEmp);
               return bl.ok(bl.employeeToJSON(validatedEmp));
            }
            else {
               // return error Response
               return bl.errorResponse("BAD_REQUEST", " Invalid field(s) input!");
            } 
         }
         else {
            return bl.errorResponse("BAD_REQUEST", " Cannot add employee " + emp_name + " for company " + company + "!");
         }      
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
         dl.close();
      }
   }

   
//    @Path("employee")
//    @PUT
//    @Produces(json)
//    public Response updateEmployee(String employee){
//      try{
//          dl = new DataLayer(emp.getCompany()); // emp doesn't have a getCompany() 
//    
//          // validate employee
//          Employee validatedEmp = bl.validateEmployee(emp, emp.getCompany(), "PUT");
//          if(bl.notNull(validatedEmp)){
//             return bl.ok(bl.employeeToJSON(validatedEmp));
//          }
//          else {
//             return bl.errorResponse("");
//          }
//
//       }
//       catch(Exception e){
//          return bl.errorResponse("ERROR", e.getMessage());
//       }
//       finally {
//          dl.close();
//       }
//     }

   
   @Path("employee")
   @DELETE
   @Produces(json)
   public Response deleteEmployee(
      @QueryParam("company") String company, 
      @QueryParam("emp_id") int emp_id
   ){
      try {
         if(bl.validateCompany(company)){
            dl = new DataLayer(company);
            
            // Get employee to make sure it exists
            Employee emp = dl.getEmployee(emp_id);
            
            if(bl.notNull(emp)){
               int rows = dl.deleteEmployee(emp_id);
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
            return bl.errorResponse("NOT_FOUND", " Employee " + emp_id + " for company " + company + " not found!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
         dl.close();
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
         if(bl.validateCompany(company)){
            dl = new DataLayer(company);
            
            List<Timecard> timecardList = dl.getAllTimecard(emp_id);
            List<String> timecards = new ArrayList<String>();
            if(bl.notNull(timecardList) && timecardList.size() > 0){
               for(int i = 0; i < timecardList.size(); i++){
                  timecards.add(bl.timecardToJSON(timecardList.get(i)));
               }
               return bl.ok(timecards);  
            }
            else {
               return bl.errorResponse("NOT_FOUND", " No timecards found for employee " + emp_id + "!");
            } 
         }
         else{
            return bl.errorResponse("NOT_FOUND", " Timecards for employee " + emp_id + " not found for company " + company + "!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally{
         dl.close();
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
         if(bl.validateCompany(company)){
            dl = new DataLayer(company);
            
            // Timecard object
            Timecard tc = dl.getTimecard(timecard_id);
            
            if(bl.notNull(tc)){
               return bl.ok(bl.timecardToJSON(tc));  
            }
            else {
               return bl.errorResponse("NOT_FOUND", " Timecard " + String.valueOf(timecard_id) + " not found!");
            } 
         }
         else{
            return bl.errorResponse("NOT_FOUND", " Timecard " + timecard_id + " not found for company " + company + "!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally{
         dl.close();
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
            // perform DL insert
            // return JSON string of newly inserted employee
            validTimecard = dl.insertTimecard(validTimecard);
            return bl.ok(bl.timecardToJSON(validTimecard));
         }
         else {
            // return error Response
            return bl.errorResponse("BAD_REQUEST", " Invalid field(s) input!");
         } 
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
         dl.close();
      }
   }



/**
   @Path("timecard")
   @PUT
   @Produces(json)
   public Response updateTimecard(String json){
      try {
         // JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
         // dl = new DataLayer();
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
         dl.close();
      }
   }

*/


   @Path("timecard")
   @DELETE
   @Produces(json)
   public Response deleteTimecard( 
      @QueryParam("company") String company,
      @QueryParam("timecard_id") int timecard_id
   ){
      try {
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
            // Not my company, don't allow
            return bl.errorResponse("BAD_REQUEST", " Timecard " + timecard_id + " not found for company " + company + "!");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally {
         dl.close();
      }
   }
   
   
     
   
}// class