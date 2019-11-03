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


public class BusinessLayer {

   // GSON to JSON builders
   GsonBuilder builder;
   Gson gson;

   /**
   *  Constructor
   */
   public BusinessLayer(){
      builder = new GsonBuilder();  // intialize GsonBuilder
      gson = builder.create();      // Create Gson object
   }
   
   /** OTHER **/
   public Response.ResponseBuilder notFound(String message, String id){
      return Response.status(Response.Status.NOT_FOUND).entity(message + id);
   }
   public Response.ResponseBuilder badRequest(String message, String id){
      return Response.status(Response.Status.BAD_REQUEST).entity(message + id);
   }
   public Response.ResponseBuilder error(Exception error){
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error: " + error);  
   }
  
   
   /**
   * departmentToJSON
   * @param Department
   * @return String
   */
   public String departmentToJSON(Department department){
      return gson.toJson(department);
   }
   
   /**
   * jsonToDepartment
   * @param String
   * @return Department
   */
   public Department jsonToDepartment(String department){
      return gson.fromJson(department, Department.class);
   }
   
   
 
   

   
   /**
   * employeeToJSON
   * @param Employee
   * @return String
   */
   public String employeeToJSON(Employee employee){
      return gson.toJson(employee);
   }
   
   /**
   * jsonToEmployee
   * @param String
   * @return Employee
   */
   public Employee jsonToEmployee(String employee){
      return gson.fromJson(employee, Employee.class);
   }
   
   
   
   
   
   

   
   /**
   * timecardToJSON
   * @param Timecard
   * @return String
   */
   public String timecardToJSON(Timecard timecard){
      return gson.toJson(timecard);
   }
   
   /**
   * jsonToTimecard
   * @param String
   * @return Timecard
   */
   public Timecard jsonToTimecard(String timecard){
      return gson.fromJson(timecard, Timecard.class);
   }
   
   
   
}