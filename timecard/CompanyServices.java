package timecard;

import companydata.*;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
// import javax.ws.rs.core.Application;
import java.util.List;
import java.util.*;

@Path("CompanyServices")
public class CompanyServices {

   @Context
   UriInfo uriInfo;
   
   // Data Layer
   DataLayer dl = null;
   
   // Business layer - validation
   BusinessLayer bl = new BusinessLayer();
    
   
   /** ---------- DEPARTMENT ---------- */
   
   // localhost:8080/MokBP2/resources/CompanyServices/departments?company={company}
   @Path("departments")
   @GET
   @Produces("application/json")
   public Response getAllDepartment(@QueryParam("company") String company){
      try {
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
   @Produces("application/json")
   public Response getDepartment(
      @QueryParam("company") String company,
      @QueryParam("dept_id") int dept_id
   ) {
      try{
         dl = new DataLayer(company);      
         
         Department dep = dl.getDepartment(company, dept_id);
    
         if(dep != null){
            // Return OK    
            return bl.ok(bl.departmentToJSON(dep));
          }
          else {
            // ERROR: Department NOT_FOUND   
            return bl.errorResponse("NOT_FOUND"," Department not found with ID: " + String.valueOf(dept_id));
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
   @Produces("application/json")
   public Response insertDepartment(
      @FormParam("dept_id") int dept_id,
      @FormParam("company") String company,
      @FormParam("dept_name") String dept_name,
      @FormParam("dept_no") String dept_no,
      @FormParam("location") String location
   ){
      try{
        dl = new DataLayer(company);
        
        // When creating new department, will use company + dept_no as dept_no needs to be unqiue across ALL companies
        String deptNo = company + dept_no;
         
        // Check to make sure it passes dept_no unique validation!         
        if(bl.validateDeptNo(company, deptNo)){
            Department newDep = null;
            
            // Check if dept_id was passed in or not to determine which constructor to make object from
            if(String.valueOf(dept_id) != "" || String.valueOf(dept_id) != null){
               // Validate that department by provided dept_id for if it doesn't already exist! 
               if(bl.validateDeptID(company, dept_id)){
                  newDep = dl.insertDepartment(new Department(dept_id, company, dept_name, dept_no, location));
                  
                  if(newDep != null){
                     // Return OK
                     return bl.ok(bl.departmentToJSON(newDep));
                  }
                  else {
                     // ERROR: New inserted department failed
                     return bl.errorResponse("INTERNAL_SERVER_ERROR", " Creating new department failed!");
                  }
               }
               else {
                  // Department ID already exists!
                  return bl.errorResponse("CONFLICT", " Department ID already exists for: " +  dept_id);
               }
            }
            else{
               // No Department ID passed
               newDep = dl.insertDepartment(new Department(company, dept_name, dept_no, location));
            }
            
            // Make sure object was created
            if(newDep != null){
               // Return OK
               return bl.ok(bl.departmentToJSON(newDep));
            }
            else {
               // ERROR: Return error
               return bl.errorResponse("INTERNAL_SERVER_ERROR"," Creating department failed!");
            }
        }
        else{
            // ERROR: Return error
            return bl.errorResponse("CONFLICT", " Department Number already exists for: " +  dept_no);
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
   @Produces("application/json")
   public Response getAllEmployee(@QueryParam("company") String company){
      try{
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
   @Produces("application/json")
   public Response getEmployee(
      @QueryParam("company") String company,
      @QueryParam("emp_id") int emp_id 
   ){
      try{
         dl = new DataLayer(company);
         
         Employee emp = dl.getEmployee(emp_id);
         if(emp != null){
            // Return OK  
            return bl.ok(bl.employeeToJSON(emp));
          }
          else {
            // ERROR: Not found  
            return bl.errorResponse("NOT_FOUND", " Employee not found with ID: " + String.valueOf(emp_id));
          }       
      }
      catch(Exception e){
         return bl.errorResponse("ERROR",e.getMessage());
      }
      finally{
         dl.close();
      }
   }     
   
   
   
   
   /** ---------- TIMECARD ---------- */
   
   // localhost:8080/MokBP2/resources/CompanyServices/timecards?company={company}&emp_id={emp_id}
//    @Path("timecards")
//    @GET
//    @Produces("application/json")
//    public Response getAllTimecard(
//       @QueryParam("company") String company,
//       @QueryParam("emp_id") int emp_id 
//    ){
//       try{
//          dl = new DataLayer(company);
//        
//          List<Timecard> timecardList = dl.getAllTimecard(emp_id);
//          List<String> timecards = new ArrayList<String>();
// 
//          for(int i = 0; i < timecardList.size(); i++){
//             timecards.add(bl.timecardToJSON(timecardList.get(i)));
//          }
//          
//          return Response.ok("{\"success\":" + timecards + "}", MediaType.APPLICATION_JSON).build();  
//       }
//       catch(Exception e){
//          return bl.errorResponse("ERROR", e.getMessage());
//       }
//       finally{
//          dl.close();
//       }
//    }
//    
//    // localhost:8080/MokBP2/resources/CompanyServices/timecard?company={company}&timecard_id={timecard_id}
//    @Path("timecard")
//    @GET
//    @Produces("application/json")
//    public Response getTimecard(
//       @QueryParam("company") String company,
//       @QueryParam("timecard_id") int timecard_id
//    ){
//       try{
//          dl = new DataLayer(company);
//          
//          // Timecard object
//          Timecard tc = dl.getTimecard(timecard_id);
//          
//          if(tc != null){
//             String tcSTR = bl.timecardToJSON(tc);
//             return Response.ok("{\"success\":" + tcSTR + "}", MediaType.APPLICATION_JSON).build();  
//          }
//          else {
//             return bl.errorResponse("NOT_FOUND", " Timecard not found with ID: " + String.valueOf(timecard_id));
//          } 
//       }
//       catch(Exception e){
//          return bl.errorResponse("ERROR",e.getMessage());
//       }
//       finally{
//          dl.close();
//       }
//    }
   
   
     
   
}// class