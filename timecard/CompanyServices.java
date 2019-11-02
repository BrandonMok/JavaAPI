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
   
   // My ID
   String id = "bxm5989";
   
   // Business layer - validation
   BusinessLayer bl = new BusinessLayer();
    
   
   // localhost:8080/MokBP2/resources/CompanyServices/departments?company={company}
   @Path("departments")
   @GET
   @Produces("application/json")
   public Response getAllDepartment(@QueryParam("company") String company){
      try {
         dl = new DataLayer(id);
         
         // Get all departments from datalayer
         List<Department> departmentsList = dl.getAllDepartment(company);
         List<String> departments = new ArrayList<String>();
         
         for(int i = 0; i < departmentsList.size(); i++){
            departments.add(bl.departmentToJSON(departmentsList.get(i)));
         }
         
         return Response.ok("{\"success\":" + departments + "}", MediaType.APPLICATION_JSON).build();
      }
      catch(Exception e){
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error: " + e).build();     
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
         dl = new DataLayer(id);
         
         Department dep = dl.getDepartment(company, dept_id);
         if(dep != null){
              String department = bl.departmentToJSON(dep);
              return Response.ok("{\"success\":" + department + "}", MediaType.APPLICATION_JSON).build();
          }
          else {
             return bl.notFound("Department not found for: ", String.valueOf(dept_id)).build();
          }
      }
      catch(Exception e){
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error: " + e).build();
      }
      finally{
         dl.close();
      }
   }
     
   
     
   
}// class