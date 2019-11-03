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
         
         return Response.ok("{\"success\":" + departments + "}", MediaType.APPLICATION_JSON).build();
      }
      catch(Exception e){
         return bl.error(e).build();     
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
              String department = bl.departmentToJSON(dep);
              return Response.ok("{\"success\":" + department + "}", MediaType.APPLICATION_JSON).build();
          }
          else {
             return bl.notFound("Department not found with ID: ", String.valueOf(dept_id)).build();
          }
      }
      catch(Exception e){
        return bl.error(e).build();    
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
         
         return Response.ok("{\"success\":" + employees + "}", MediaType.APPLICATION_JSON).build();  
      }
      catch(Exception e){
         return bl.error(e).build(); 
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
              String employee = bl.employeeToJSON(emp);
              return Response.ok("{\"success\":" + employee + "}").build();
          }
          else {
             return bl.notFound("Employee not found with ID: ", String.valueOf(emp_id)).build();
          }       
      }
      catch(Exception e){
         return bl.error(e).build();
      }
      finally{
         dl.close();
      }
   }     
   
     
   
}// class