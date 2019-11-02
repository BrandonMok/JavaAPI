package timecard;

import javax.ws.rs.core.*;
import javax.ws.rs.*;

public class BusinessLayer {

   /**
   *  Constructor
   */
   public BusinessLayer(){

   }
   
   
   
   /** ---------- DEPARTMENT ---------- **/
   
   /**
   * departmentToJSON
   * @param Department
   * @return String
   */
   public String departmentToJSON(Department department){}
   
   public Department jsonToDepartment(String department){}
   
   
   
   
   
   /** ---------- EMPLOYEE  ---------- **/
   
   /**
   * employeeToJSON
   * @param Employee
   * @return String
   */
   public String employeeToJSON(Employee employee){}
   
   public Employee jsonToEmployee(String employee){}
   
   
   
   
   
   
   /** ---------- TIMECARD ---------- **/
   
   /**
   * timecardToJSON
   * @param Timecard
   * @return String
   */
   public String timecardToJSON(Timecard timecard){}
   
   public Timecard jsonToTimecard(String timecard){}
   
   
   
}