package timecard;

import companydata.*;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
// import javax.ws.rs.core.Application;
import java.util.List;

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
   

   @Path("departments")
   @GET
   @Produces("application/json")
   public Response getAllDepartment(@QueryParam("company") String company){
      try {
         dl = new DataLayer(id);
         
         // Get all departments from datalayer
         List<Department> departmentsList = dl.getAllDepartment(company);
         List<String> departments = null;
         
         for(int i = 0; i < departmentsList.size(); i++){
            departments.add(bl.departmentToJSON(departmentsList.get(i)));
         }
         
         // return departments; // return list of department object
         return Response.ok("{\"success\":\"" + departments + "}").build();
      }
      catch(Exception e){
          return Response.ok("{\"error\":\"no departments found!\"}").build();
      }
      finally{
         dl.close();
      }
   }
   
   
   
        
//    @Path("department/{departmentID}")
//    @GET
//    @Produces("application/json")
//    public Department getDepartment(String companyName, int dept_id) {
//       //  return Response.ok("{\"response\":\"Hello\"}").build();
//    }
   
   
   
     
   
}// class