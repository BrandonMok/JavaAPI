package timecard;

import javax.ws.rs.core.*;
import javax.ws.rs.*;
import companydata.*;

@Path("CompanyServices")
public class CompanyServices {

   @Context
   UriInfo uriInfo;
   
   // Data Layer
   DatalLayer dl = null;
   
   // My ID
   String id = "bxm5989";
   
   // Business layer - validation
   BusinessLayer bl = new BusinessLayer();
   

   @Path("departments")
   @GET
   @Produces("application/json")
   public List<Department> getAllDepartment(QueryParam("company") String companyName){
      try {
         dl = new DataLayer(id);
         
         List<Department> departmentsList = dl.getAllDepartment(companyName);
         
         // do businesslayer validation!
         //return Response.ok().build();// 
      }
      catch(Exception e){
         System.out.println("Problem with query: "+ e.getMessage());
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