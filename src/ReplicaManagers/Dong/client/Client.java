package ReplicaManagers.Dong.client;

import static ReplicaManagers.Dong.utils_client.Utils.getValidAppointmentId;
import static ReplicaManagers.Dong.utils_client.Utils.getValidAppointmentType;
import static ReplicaManagers.Dong.utils_client.Utils.getValidPatientId;

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

import ReplicaManagers.Dong.corbasystem_RM.IServer;
import ReplicaManagers.Dong.corbasystem_RM.IServerHelper;
import ReplicaManagers.Dong.utils_client.Utils;

public class Client {

   private static String appointmentId;
   private static String appointmentType;
   private static String capacity;
   private static String patientId;
   private static String userType;
   private static String operatorId;
   private static final Logger loggerClient = Logger.getLogger("ReplicaManagers/Dong/client");
   private static final Logger loggerUser = Logger.getLogger("user");

   public static void main(String args[]) {

      try {
         loggerClient.log(Level.INFO, "Enter your user id to start with:");
         InputStreamReader is = new InputStreamReader(System.in);
         BufferedReader br = new BufferedReader(is);

         //Get a valid User Id
         String userID = br.readLine();
         while (!Utils.isValidUserId(userID)) {
            loggerClient.log(Level.WARNING, "Please input a valid user id.");
            userID = br.readLine();
         }

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
         String targetCity = Utils.getCity(userID);
         Properties props = new Properties();
         //Generate and initiate the ORB
         props.put("org.omg.CORBA.ORBInitialPort", "1050");
         props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
         ORB orb = ORB.init(args, props);
         // Get Root naming ReplicaManagers.Dong.server
         org.omg.CORBA.Object objRef = null;
         objRef = orb.resolve_initial_references("NameService");

         NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

         // Get object reference through naming ReplicaManagers.Dong.server
         IServer server = null;
         server = IServerHelper.narrow(ncRef.resolve_str(targetCity));
         loggerClient.log(Level.INFO, "Lookup completed.");

         userType = Utils.getUserType(userID);
         if (userType.equals(Utils.ADMIN)) {
            /**
             * Admins Operations
             */
            loggerClient.log(Level.INFO, Utils.mainMsg(userType, userID));
            String userInput = Utils.getValidInput(br);
            while (!userInput.equals("0")) {
               String opsRes = adminActions(br, userInput, server, targetCity);
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
               String opsRes = patientActions(br, userInput, server);
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
   private static String patientActions(BufferedReader br, String userSelection, IServer server) throws IOException {
      switch (userSelection.toLowerCase()) {
         case "a":
            patientId = getValidPatientId(br);

            appointmentId = getValidAppointmentId(br, Utils.APP_ID);

            appointmentType = getValidAppointmentType(br, Utils.APP_TYPE);

            return server.bookAppointment(patientId, appointmentId, appointmentType);
         case "b":
            patientId = getValidPatientId(br);

            return server.getAppointmentSchedule(patientId);
         case "c":
            patientId = getValidPatientId(br);

            if (userType.equals(Utils.PATIENT) && !patientId.equalsIgnoreCase(operatorId)) {
               return "You are not able to cancel the appointment which does not belongs to you.";
            }
            appointmentId = getValidAppointmentId(br, Utils.APP_ID);

            return server.cancelAppointment(patientId, appointmentId);
         case "d":
            patientId = getValidPatientId(br);
            String oldAppointmentId = getValidAppointmentId(br, Utils.OLD_APP_ID);
            String newAppointmentId = getValidAppointmentId(br, Utils.NEW_APP_ID);
            String oldAppointmentType = getValidAppointmentType(br, Utils.OLD_APP_TYPE);
            String newAppointmentType = getValidAppointmentType(br, Utils.NEW_APP_TYPE);
            return server.swapAppointment(patientId, oldAppointmentId, oldAppointmentType, newAppointmentId, newAppointmentType);
         default:
            return "Could not go here, no such a selection in patient menu!";
      }
   }

   private static String adminActions(BufferedReader br, String userSelection, IServer server, String targetCity) throws IOException {
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
            return server.addAppointment(appointmentId, realType, capacity);
         case "2":

            appointmentId = getValidAppointmentId(br, Utils.APP_ID);

            do {
               appointmentType = getValidAppointmentType(br, Utils.APP_TYPE);
            }
            while (Utils.getRealAppType(appointmentType).equals(Utils.WRONG));
            return server.removeAppointment(appointmentId, Utils.getRealAppType(appointmentType));
         case "3":
            do {
               appointmentType = getValidAppointmentType(br, Utils.APP_TYPE);
            }
            while (Utils.getRealAppType(appointmentType).equals(Utils.WRONG));
            return server.listAppointmentAvailability(Utils.getRealAppType(appointmentType));
         case "4":
            return server.initiateServer(targetCity);
         case "a":
         case "b":
         case "c":
         case "d":
            return patientActions(br, userSelection, server);
         default:
            return "Could not go here, no such a selection in admin menu!";
      }
   }
}
