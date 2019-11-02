package timecard;

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
   
   
   
   /** ---------- DEPARTMENT ---------- **/
   
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
      return gson.fromJson(IOUtils.toString(department,null), Department.class);
   }
   
   
   
   
   
   /** ---------- EMPLOYEE  ---------- **/
   
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
      return gson.fromJson(IOUtils.toString(employee,null), Employee.class);
   }
   
   
   
   
   
   
   /** ---------- TIMECARD ---------- **/
   
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
      return gson.fromJson(IOUtils.toString(timecard,null), Timecard.class);
   }
   
   
   
}