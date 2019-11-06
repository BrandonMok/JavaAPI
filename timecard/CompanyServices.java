package timecard;

import companydata.*;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
// import java.util.List;
import java.util.*;

@Path("CompanyServices")
public class CompanyServices {

   @Context
   UriInfo uriInfo;
   
   // Data Layer
   DataLayer dl = null;
   
   // Produces JSON string - ALL functions return json
   public static final String json = "application/json";
   
   // Business layer - validation
   BusinessLayer bl = new BusinessLayer();
    
   
   /** ---------- DEPARTMENT ---------- */
   
   // localhost:8080/MokBP2/resources/CompanyServices/departments?company={company}
   @Path("departments")
   @GET
   @Produces(json)
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
   @Produces(json)
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
   @Produces(json)
   public Response insertDepartment(
      @FormParam("dept_id") int dept_id,
      @FormParam("company") String company,
      @FormParam("dept_name") String dept_name,
      @FormParam("dept_no") String dept_no,
      @FormParam("location") String location
   ){
      try{
        dl = new DataLayer(company);
        
        // Determine to make sure department name is unique
        // Allow for user to apply company + dept_no or if not, then do it here
        String uniqueDep = bl.uniquePerCompany(dept_no, company);
               
        // Check to make sure it passes dept_no unique validation!     
        if(!uniqueDep.equals("")){    
           if(bl.validateDeptNo(company, uniqueDep)){
               Department newDep = newDep = dl.insertDepartment(new Department(dept_id, company, dept_name, uniqueDep, location));
               
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
         else {
            // ERROR: Company isn't mine
            return bl.errorResponse("BAD_REQUEST", " Company entered isn't allowed");
         }
      }
      catch(Exception e){
         return bl.errorResponse("ERROR", e.getMessage());
      }
      finally{
         dl.close();
      }
   }
   
   // @Path("department")
//    @PUT
//    @Consumes(json)
//    @Produces(json)
//    public Response updateDepartment(Department dep){
//       try{
//          dl = new DataLayer(dep.getCompany());
//          
//          // 1) ID needs to be an existing one - look for it if exists
//          // 2) Dept_No needs to be unique!   - will be in the list so do check to add bxm5989 + dep_no
//          
//          // ID needs to be an existing one
//          if(bl.validateDeptID()){
//             
//          }
//          else {
//          
//          }
//          
//          
//          
//          
//          
//       }
//       catch(Exception e){
//          return bl.errorResponse("ERROR", e.getMessage());
//       }
//       finally{
//          dl.close();
//       }
//    }
   
   @Path("department")
   @DELETE
   @Produces(json)
   public Response deleteDepartment(
      @QueryParam("company") String company,
      @QueryParam("dept_id") int dept_id
   ){
      try{
         dl = new DataLayer(company);
          
         // deleteDepartment in DL takes a String of company name
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
   @Produces(json)
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
 
 /**  
   @Path("employee")
   @POST
   @Produces(json)
   public Response insertEmployee(
      @FormParam("company") String company,
      @FormParam("emp_name") String emp_name,
      @FormParam("emp_no") String emp_no,
      @FormParam("hire_date") Date hire_date,  
      @FormParam("job") String job,
      @FormParam("salary") double salary, 
      @FormParam("dept_id") int dept_id,
      @FormParam("mng_id") int mng_id,
      @FormParam("emp_id") int emp_id
   ){
      try{
         dl = new DataLayer(company);
         String uniqueEmpNo = bl.uniquePerCompany(emp_no, company);
         
         // CHECK if emp_id was passed or not
         if(String.valueOf(emp_id) != "" || String.valueOf(emp_id) != null){
            
         }
         else{
            // emp_id not passed, find lastID
         }
            
      
         Map<String, String> map = new HashMap<String, String>();
            map.add("emp_name", emp_name);
            map.add("emp_no", emp_no);
            map.add("hire_date", hire_date);
            map.add("job", job);
            map.add("salary", salary);
            map.add("dept_id", String.valueOf(dept_id));
            map.add("mng_id", String.valueOf(mng_id));
            
         Employee emp = new Employee();
         
         
         
         
      }
      catch(Exception e){
      
      }
      finally {
         dl.close();
      }
   }
   */
   
   
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
         dl = new DataLayer(company);
       
         List<Timecard> timecardList = dl.getAllTimecard(emp_id);
         List<String> timecards = new ArrayList<String>();

         for(int i = 0; i < timecardList.size(); i++){
            timecards.add(bl.timecardToJSON(timecardList.get(i)));
         }
         
         return Response.ok("{\"success\":" + timecards + "}", MediaType.APPLICATION_JSON).build();  
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
         dl = new DataLayer(company);
         
         // Timecard object
         Timecard tc = dl.getTimecard(timecard_id);
         
         if(tc != null){
            String tcSTR = bl.timecardToJSON(tc);
            return Response.ok("{\"success\":" + tcSTR + "}", MediaType.APPLICATION_JSON).build();  
         }
         else {
            return bl.errorResponse("NOT_FOUND", " Timecard not found with ID: " + String.valueOf(timecard_id));
         } 
      }
      catch(Exception e){
         return bl.errorResponse("ERROR",e.getMessage());
      }
      finally{
         dl.close();
      }
   }
   
   
     
   
}// class