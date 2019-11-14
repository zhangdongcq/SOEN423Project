package ReplicaManagers.Dong.utils_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {
   public static final String APP_TYPE_PHYSICIAN = "Physician";
   public static final String APP_TYPE_SURGEON = "Surgeon";
   public static final String APP_TYPE_DENTAL = "Dental";
   public static final String WRONG = "Wrong";
   public static final String ADMIN = "Admins";
   public static final String PATIENT = "Patients";
   public static final String APP_ID = "appointmentId";
   public static final String APP_TYPE = "appointmentType";
   public static final String OLD_APP_ID = "old appointmentId";
   public static final String OLD_APP_TYPE = "old appointmentType";
   public static final String NEW_APP_TYPE = "new appointmentType";
   public static final String NEW_APP_ID = "new appointmentId";
   public static final String MTL = "mtl";
   public static final String QUE = "que";
   public static final String SHE = "she";
   private static final Logger loggerClient = Logger.getLogger("ReplicaManagers/Dong/client");

   public static String mainMsg(String userType, String userId) {
      if (userType.equals(ADMIN)) {
         StringBuilder sb = new StringBuilder();
         sb.append("\n");
         sb.append("********Hello, Admin: ").append(userId).append("****************************").append("\n");
         sb.append("Please select one of follow options, input number (0 - 4, a - c):").append("\n");
         sb.append(adminMsg());
         sb.append(patientMsg());
         sb.append("*****************************************************************");
         return sb.toString();
      }
      if (userType.equals(PATIENT)) {
         StringBuilder sb = new StringBuilder();
         sb.append("\n");
         sb.append("********Hello, Patient: ").append(userId).append("****************************").append("\n");
         sb.append("Please select one of follow options, input number (a - d):").append("\n");
         sb.append(patientMsg());
         sb.append("*****************************************************************");
         return sb.toString();
      }
      return "Should not be here! Main MSG";
   }

   private static String patientMsg() {
      StringBuilder sb = new StringBuilder();
      sb.append("\n");
      sb.append("* a: bookAppointment(patientID, appointmentID, appointmentType)").append("\n")
              .append("* b: getAppointmentSchedule(patientID)").append("\n")
              .append("* c: cancelAppointment(patientID, appointmentId)").append("\n")
              .append("* d: swapAppointment(patientID, oldAppointmentID, oldAppointmentType, newAppointmentID, newAppointmentType)").append("\n")
              .append("* 0: Quit").append("\n");
      return sb.toString();
   }

   private static String adminMsg() {
      StringBuilder sb = new StringBuilder();
      sb.append("\n");
      sb.append("* 1: addAppointment(appointmentID, appointmentType, capacity)").append("\n")
              .append("* 2: removeAppointment(appointmentID, appointmentType)").append("\n")
              .append("* 3: listAppointmentAvailability(appointmentType)").append("\n")
              .append("* 4: Initiate System").append("\n");
      return sb.toString();
   }

   public static void askForAppointmentID(String reminder) {
      loggerClient.log(Level.INFO, String.format("Please input the %s:", reminder));
   }

   public static void askForAppointmentType(String reminder) {
      loggerClient.log(Level.INFO, String.format("Please input the %s (1 - Physician, 2 - Surgeon, 3 - Dental):", reminder));
   }

   public static void askForCapacity() {
      loggerClient.log(Level.INFO, "Please input the capacity:");
   }

   public static void askForPatientID() {
      loggerClient.log(Level.INFO, "Please input the patient id:");
   }

   public static String getRealAppType(String input) {
      switch (input) {
         case "1":
            return APP_TYPE_PHYSICIAN;
         case "2":
            return APP_TYPE_SURGEON;
         case "3":
            return APP_TYPE_DENTAL;
         default:
            return WRONG;
      }
   }

   public static String getUserType(String userID) {
      switch (userID.toUpperCase().charAt(3)) {
         case 'A':
            return ADMIN;
         case 'P':
            return PATIENT;
         default:
            return WRONG;
      }
   }

   public static String getServerURL(String city, Integer portNumber) {
      return "rmi://localhost:" + portNumber + "/" + city;
   }

   public static String getCity(String str) {
      switch (str.toUpperCase().substring(0, 3)) {
         case "MTL":
            return "mtl";
         case "QUE":
            return "que";
         case "SHE":
            return "she";
         default:
            return WRONG;
      }
   }

   public static String getServerURL(String city) {
      return getServerURL(city, 9999);
   }

   /**
    * This method is supposed
    *
    * @param userId
    * @return
    */
   public static boolean isValidUserId(String userId) {
      return userId.length() == 8 && !getCity(userId).equals(WRONG) && !getUserType(userId).equals(WRONG);
   }

   public static boolean isValidUserInput(String userInput) {
      return userInput.equals("0")
              || userInput.equals("1")
              || userInput.equals("2")
              || userInput.equals("3")
              || userInput.equals("4")
              || userInput.equalsIgnoreCase("a")
              || userInput.equalsIgnoreCase("b")
              || userInput.equalsIgnoreCase("c")
              || userInput.equalsIgnoreCase("d");
   }

   public static String getValidInput(BufferedReader br) {
      String userInput = null;
      try {
         userInput = br.readLine();
      } catch (IOException e) {
         e.printStackTrace();
      }
      while (Objects.isNull(userInput) || !Utils.isValidUserInput(userInput.trim())) {
         loggerClient.log(Level.WARNING, "Please input a valid number.");
         try {
            userInput = br.readLine();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      return userInput;
   }

   public static String getValidAppointmentId(BufferedReader br, String reminder) throws IOException {
      askForAppointmentID(reminder);
      String appointmentId = br.readLine();
      while (!isValidAppointmentId(appointmentId)) {
         loggerClient.log(Level.WARNING, "Invalid appointment id! Please enter another one.");
         appointmentId = br.readLine();
      }
      return appointmentId;
   }

   private static boolean isValidPatientId(String patientId) {
      return patientId.length() == 8
              && (patientId.substring(0, 3).equalsIgnoreCase(MTL)
              || patientId.substring(0, 3).equalsIgnoreCase(QUE)
              || patientId.substring(0, 3).equalsIgnoreCase(SHE))
              && patientId.charAt(3) == 'P';
   }

   public static String getValidPatientId(BufferedReader br) throws IOException {
      askForPatientID();
      String patientId = br.readLine();
      while (!isValidPatientId(patientId.toUpperCase())) {
         loggerClient.log(Level.WARNING, "Invalid patient id! Please enter another one.");
         patientId = br.readLine();
      }
      return patientId;
   }

   private static boolean isValidAppointmentId(String appointmentId) {
      return appointmentId.length() == 10
              && (appointmentId.substring(0, 3).equalsIgnoreCase(MTL)
              || appointmentId.substring(0, 3).equalsIgnoreCase(QUE)
              || appointmentId.substring(0, 3).equalsIgnoreCase(SHE))
              && (appointmentId.charAt(3) == 'M'
              || appointmentId.charAt(3) == 'A'
              || appointmentId.charAt(3) == 'E');
   }


   public static String getValidAppointmentType(BufferedReader br, String reminder) throws IOException {
      askForAppointmentType(reminder);
      String appointType = br.readLine();
      while (!isValidAppointmentType(appointType)) {
         loggerClient.log(Level.WARNING, "Invalid appointment type! Please enter a valid number.");
         appointType = br.readLine();
      }
      return appointType;
   }

   public static boolean isValidAppointmentType(String appointmentTypeNumber) {
      return appointmentTypeNumber.equalsIgnoreCase("1")
              || appointmentTypeNumber.equalsIgnoreCase("2")
              || appointmentTypeNumber.equalsIgnoreCase("3");
   }
}
