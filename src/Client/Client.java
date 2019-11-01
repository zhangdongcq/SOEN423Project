package Client;

import static Client.Utils.getCity;
import static Client.Utils.getValidAppointmentId;
import static Client.Utils.getValidAppointmentType;
import static Client.Utils.getValidPatientId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import corbasystem.IFrontEndServer;
import corbasystem.IFrontEndServerHelper;

public class Client {

   private static String appointmentId;
   private static String appointmentType;
   private static String capacity;
   private static String patientId;
   private static String userID;
   private static String userType;
   private static String frontEndServerName;
   private static String operatorId;
   private static final Logger loggerClient = Logger.getLogger("client");
   private static final Logger loggerUser = Logger.getLogger("user");

   public static void main(String args[]) {

      try {
         loggerClient.log(Level.INFO, "Enter your user id to start with:");
         InputStreamReader is = new InputStreamReader(System.in);
         BufferedReader br = new BufferedReader(is);

         //Get a valid User Id
         userID = br.readLine();
         while (!Utils.isValidUserId(userID)) {
            loggerClient.log(Level.WARNING, "Please input a valid user id.");
            userID = br.readLine();
         }

         //Get Frontend server name
         frontEndServerName = Utils.getCity(userID);

         //Setup FileHandler for Logging in file
         try {
            FileHandler fileHandler = new FileHandler(userID + ".log");
            fileHandler.setLevel(Level.INFO);
            loggerUser.addHandler(fileHandler);
         } catch (IOException e) {
            loggerClient.log(Level.SEVERE, "File logger is not working.", e);
         }

         /**
          * Setup connection
          */
         operatorId = userID;

         Properties props = new Properties();
         //Generate and initiate the ORB
         props.put("org.omg.CORBA.ORBInitialPort", "1050");
         props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
         ORB orb = ORB.init(args, props);
         // Get Root naming server
         org.omg.CORBA.Object objRef = null;
         objRef = orb.resolve_initial_references("NameService");

         NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

         // Get object reference through naming server
         IFrontEndServer frontEndServer = IFrontEndServerHelper.narrow(ncRef.resolve_str(frontEndServerName));
         loggerClient.log(Level.INFO, "Lookup completed.");

         userType = Utils.getUserType(userID);
         if (userType.equals(Utils.ADMIN)) {
            /**
             * Admins Operations
             */
            loggerClient.log(Level.INFO, Utils.mainMsg(userType, userID));
            String userInput = Utils.getValidInput(br);
            while (!userInput.equals("0")) {
               String opsRes = adminActions(userID, br, userInput, frontEndServer, getCity(userID));//city-specified ops
               loggerUser.log(Level.INFO, opsRes);
               loggerClient.log(Level.INFO, Utils.mainMsg(userType, userID));
               userInput = Utils.getValidInput(br);
            }
         } else if (userType.equals(Utils.PATIENT)) {
            /**
             * Patients Operations
             */
            loggerClient.log(Level.INFO, Utils.mainMsg(userType, userID));
            String userInput = Utils.getValidInput(br);
            while (!userInput.equals("0")) {
               String opsRes = patientActions(userID, br, userInput, frontEndServer);
               loggerUser.log(Level.INFO, opsRes);
               loggerClient.log(Level.INFO, Utils.mainMsg(userType, userID));
               userInput = Utils.getValidInput(br);
            }
         } else {
            loggerClient.log(Level.SEVERE, "Wrong user type from your user id! Bye.");
         }

      } catch (InvalidName | IOException | CannotProceed | NotFound | org.omg.CosNaming.NamingContextPackage.InvalidName invalidName) {
         invalidName.printStackTrace();
      }
   } //end main

   /**
    * Sub functions
    */
   private static String patientActions(String userId, BufferedReader br, String userSelection, IFrontEndServer frontEndServer) throws IOException {
      switch (userSelection.toLowerCase()) {
         case "a":
            patientId = getValidPatientId(br);

            appointmentId = getValidAppointmentId(br, Utils.APP_ID);

            appointmentType = getValidAppointmentType(br, Utils.APP_TYPE);

            String realAppType = Utils.getRealAppType(appointmentType);

            return frontEndServer.requestHandler(userId, "bookAppointment", String.join(",", patientId, appointmentId, realAppType));
         case "b":
            patientId = getValidPatientId(br);

            return frontEndServer.requestHandler(userId, "getAppointmentSchedule", patientId);
         case "c":
            patientId = getValidPatientId(br);

            if (userType.equals(Utils.PATIENT) && !patientId.equalsIgnoreCase(operatorId)) {
               return "You are not able to cancel the appointment which does not belongs to you.";
            }
            appointmentId = getValidAppointmentId(br, Utils.APP_ID);

            return frontEndServer.requestHandler(userId, "cancelAppointment", String.join(",", patientId, appointmentId));
         case "d":
            patientId = getValidPatientId(br);
            String oldAppointmentId = getValidAppointmentId(br, Utils.OLD_APP_ID);

            String newAppointmentId = getValidAppointmentId(br, Utils.NEW_APP_ID);

            String oldAppointmentType = getValidAppointmentType(br, Utils.OLD_APP_TYPE);
            oldAppointmentType = Utils.getRealAppType(oldAppointmentType);

            String newAppointmentType = getValidAppointmentType(br, Utils.NEW_APP_TYPE);
            newAppointmentType = Utils.getRealAppType(newAppointmentType);
            return frontEndServer.requestHandler(userId, "swapAppointment", String.join(",", patientId, oldAppointmentId, oldAppointmentType, newAppointmentId, newAppointmentType));
         default:
            return "Could not go here, no such a selection in patient menu!";
      }
   }

   private static String adminActions(String userId, BufferedReader br, String userSelection, IFrontEndServer frontEndServer, String targetCity) throws IOException {
      switch (userSelection) {
         case "1":
            appointmentId = getValidAppointmentId(br, Utils.APP_ID);

            appointmentType = getValidAppointmentType(br, Utils.APP_TYPE);

            Utils.askForCapacity();
            capacity = br.readLine();
            String realType = Utils.getRealAppType(appointmentType);
            if (!appointmentId.substring(0, 3).equalsIgnoreCase(targetCity)) {
               return String.format("Please only add your city-specific appointment. City: %s(A/E/M)XXXXXX.", targetCity.toUpperCase());
            }
            return frontEndServer.requestHandler(userId, "addAppointment", String.join(",", appointmentId, realType, capacity));

         case "2":

            appointmentId = getValidAppointmentId(br, Utils.APP_ID);

            do {
               appointmentType = getValidAppointmentType(br, Utils.APP_TYPE);
            }
            while (Utils.getRealAppType(appointmentType).equals(Utils.WRONG));
            return frontEndServer.requestHandler(userId, "removeAppointment", String.join(",", appointmentId, Utils.getRealAppType(appointmentType)));

         case "3":
            do {
               appointmentType = getValidAppointmentType(br, Utils.APP_TYPE);
            }
            while (Utils.getRealAppType(appointmentType).equals(Utils.WRONG));
            return frontEndServer.requestHandler(userId, "listAppointmentAvailability", Utils.getRealAppType(appointmentType));

         case "a":
         case "b":
         case "c":
         case "d":
            return patientActions(userId, br, userSelection, frontEndServer);
         default:
            return "Could not go here, no such a selection in admin menu!";
      }
   }
}
