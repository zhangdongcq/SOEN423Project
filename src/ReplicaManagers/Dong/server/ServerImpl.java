package ReplicaManagers.Dong.server;

import static ReplicaManagers.Dong.utils_server.Utils.getCityFromAppointmentID;
import static ReplicaManagers.Dong.utils_server.Utils.getTotalResponse;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ReplicaManagers.Dong.corbasystem_RM.IServerPOA;
import ReplicaManagers.Dong.utils_server.Utils;

public class ServerImpl extends IServerPOA {
   private static final long serialVersionUID = 4077329331765423123L;
   private String serverName;
   private Map<String, Map<String, List<String>>> cityRecords = new ConcurrentHashMap<>();
   private Map<String, Integer> patientsBookingCount = new HashMap<>();
   private List<String> adminRecords = new ArrayList<>();
   private boolean sysLock = false;
   private static final Logger logger = Logger.getLogger("ReplicaManagers/Dong/server");
   private static final String FAIL = "FAIL";
   private static final String SUCCESS = "SUCCESS";

   @Override
   public synchronized String initiateServer(String city) {
      if (isSysLock()) return "The ReplicaManagers.Dong.server is already initiated! Not able to do again!\n";
      Utils.initiateServer(cityRecords, this.getServerName(), patientsBookingCount, adminRecords);
      setSysLock(true);
      return String.format("Hello, this is ReplicaManagers.Dong.server %s, I have completed ReplicaManagers.Dong.server initiation.%n", city.toUpperCase());
   }

   @Override
   public synchronized String addAppointment(String appointmentID, String appointmentType, String capacity) {
      List<String> appDetail = new ArrayList<>();
      appDetail.add(capacity);
      Map<String, List<String>> appIDMap = new HashMap<>();
      appIDMap.put(appointmentID, appDetail);

      if (!cityRecords.containsKey(appointmentType)) {
         cityRecords.put(appointmentType, appIDMap);
         return String.format("You have successfully added an appointment ID %s under appointment type %s!", appointmentID, appointmentType);
      }
      if (cityRecords.get(appointmentType).containsKey(appointmentID)) {
         return FAIL;
//         return String.format("The given appointment ID %s is already exist in appointment type %s!", appointmentID, appointmentType);
      }
      cityRecords.get(appointmentType).put(appointmentID, appDetail);
      return String.format("You have successfully added an appointment ID %s under appointment type %s!", appointmentID, appointmentType);
   }

   @Override
   public synchronized String removeAppointment(String appointmentID, String appointmentType) {
      if (appointmentID == null || appointmentType == null || appointmentID.isEmpty() || appointmentType.isEmpty()) {
         return FAIL;
//         return "Illegal parameter(s), is null or missing!";
      }
      if (!cityRecords.containsKey(appointmentType)) {
         return FAIL;
//         return "No such an appointment type in record!";
      }
      if (!cityRecords.get(appointmentType).containsKey(appointmentID)) {
         return FAIL;
//         return String.format("No such an appointment ID %s under given appointment type %s", appointmentID, appointmentType);
      }
      if (cityRecords.get(appointmentType).get(appointmentID).size() == 1) {
         cityRecords.get(appointmentType).remove(appointmentID);
         return String.format("The appointment id %s has been removed from appointment type %s.", appointmentID, appointmentType);
//         return String.format("The appointment id %s has been removed from appointment type %s.", appointmentID, appointmentType);
      }
      if (cityRecords.get(appointmentType).get(appointmentID).size() > 1) {
         return "Not able to remove the appointment, as there are patients inside, please contact the patients to make another appoint.";
      }
      return "Should not be here: admin - removeAppointment";
   }

   @Override
   public synchronized String bookAppointment(String patientID, String appointmentID, String appointmentType) {
      if (patientID.isEmpty() || appointmentID.isEmpty() || appointmentType.isEmpty()) {
         return FAIL;
//         return "Missing Parameters!";
      }
      String numberType = appointmentType;
      appointmentType = Utils.getRealAppType(appointmentType);
      try {
         if (!getCityFromAppointmentID(appointmentID).equalsIgnoreCase(this.serverName)) { // book remote ReplicaManagers.Dong.server app
            return Utils.getRemoteServerQueryResult(Utils.BOOK_REMOTE_APP, String.join(".", patientID.toUpperCase(), appointmentID, numberType), getCityFromAppointmentID(appointmentID).toLowerCase());
         } else {
            if (!cityRecords.containsKey(appointmentType)) {
               return FAIL;
//               return String.format("No local record for appointment type %s in ReplicaManagers.Dong.server %s yet.", appointmentType, this.getServerName());
            }
            if (!cityRecords.get(appointmentType).containsKey(appointmentID)) {
               return FAIL;
//               return String.format("No local record for appointment id %s in ReplicaManagers.Dong.server %s yet.", appointmentID, this.getServerName());
            }
            List<String> appDetail = cityRecords.get(appointmentType).get(appointmentID);

            if (Integer.parseInt(appDetail.get(0)) == 0) {
               return FAIL;
//               return String.format("No more available place to register under this appointment id %s in ReplicaManagers.Dong.server %s", appointmentID, this.getServerName());
            }
            appDetail.set(0, String.valueOf(Integer.parseInt(appDetail.get(0)) - 1));
            appDetail.add(patientID.toUpperCase());
            // Records each patient
            patientsBookingCount.put(patientID, patientsBookingCount.getOrDefault(patientID, 1) + 1);
            return String.format("You, patient ID %s has successfully reserved a place under appointment ID %s in ReplicaManagers.Dong.server %s", patientID.toUpperCase(), appointmentID, this.getServerName());
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      return "bookAppointment - ServerImpl";
   }

   @Override
   public synchronized String cancelAppointment(String patientID, String appointmentID) {
      if (patientID.isEmpty() || appointmentID.isEmpty()) {
         return FAIL;
//         return "No, missing Parameters!";
      }
      if (cityRecords.size() == 0) {
         return FAIL;
//         return "No record in system which might be not initiated yet!";
      }
      // Validate the appointment id
      boolean doesAppointmentIdExist =
              cityRecords.entrySet().stream().anyMatch(entry -> entry.getValue().get(appointmentID) != null);
      if (!doesAppointmentIdExist) {
         return FAIL;
//         return String.format("No, the appointment id %s does not exist in system of city %s.", appointmentID, this.getServerName());
      }

      // Validate the patient id
      boolean doesPatientIdExist =
              cityRecords.entrySet().stream().anyMatch(entry -> entry.getValue().get(appointmentID).contains(patientID));
      if (!doesPatientIdExist) {
         return String.format("No, the patient ID %s does not exist in city %s!", patientID, this.getServerName());
      }

      // Do work
      List<String> appDetail;
      for (Map<String, List<String>> element : cityRecords.values()) {
         if (element.containsKey(appointmentID) && element.get(appointmentID).contains(patientID)) {
            appDetail = element.get(appointmentID);
            appDetail.remove(patientID);
            element.get(appointmentID).set(0, (Integer.parseInt(appDetail.get(0)) + 1) + "");
         }
      }
      return String.format("You, patient ID %s has successfully cancelled a place under appointment ID %s in ReplicaManagers.Dong.server %s", patientID, appointmentID, this.getServerName());
   }

   @Override
   public String swapAppointment(String patientID, String oldAppointmentID, String oldAppointmentType, String newAppointmentID, String newAppointmentType) {
      String response = "";
      /**
       * Check local record for given patient ID
       */
      oldAppointmentType = Utils.getRealAppType(oldAppointmentType);
      if (!cityRecords.containsKey(oldAppointmentType))
         return String.format("No record for the old appointment type %s in ReplicaManagers.Dong.server %s", oldAppointmentType, this.getServerName());
      if (!cityRecords.get(oldAppointmentType).containsKey(oldAppointmentID))
         return String.format("No record for old appointment id %s under old appointment type %s for ReplicaManagers.Dong.server city %s", oldAppointmentID, oldAppointmentType, this.getServerName());
      if (!cityRecords.get(oldAppointmentType).get(oldAppointmentID).contains(patientID))
         return String.format("No record for patient id %s under old appointment id %s of old appointment type %s for ReplicaManagers.Dong.server city %s", patientID, oldAppointmentID, oldAppointmentType, this.getServerName());

      /**
       * UDP Client [mtl, que, she]
       * Check remote Availability
       */
      synchronized (this) {
         response = this.bookAppointment(patientID, newAppointmentID, newAppointmentType);
         if (response.startsWith("No")) return response;
         response = this.cancelAppointment(patientID, oldAppointmentID) + ", meanwhile, " + response;
      }

      return response;
   }

   public String getServerName() {
      return serverName;
   }

   public synchronized void setServerName(String serverName) {
      this.serverName = serverName;
   }

   private synchronized boolean isSysLock() {
      return sysLock;
   }

   private synchronized void setSysLock(boolean sysLock) {
      this.sysLock = sysLock;
   }

   public String getLocalAppointmentAvailability(String appointmentType) {
      if (!cityRecords.containsKey(appointmentType)) {
         return "";
      }
      Map<String, String> response = new HashMap<>();
      //{MTLE131119=3, MTLE141119=6} ==> MTLE131119 3, MTLE141119 6
      cityRecords.get(appointmentType).forEach((appId, strings) -> response.put(appId, strings.get(0)));
      return response.isEmpty() ? "" : response.toString()
              .replace("=", " ")
              .replace("{", " ")
              .replace("}", " ")
              .trim();
   }

   @Override
   public String listAppointmentAvailability(String appointmentType) {

      String localResponse = getLocalAppointmentAvailability(appointmentType);
      String remoteResponse1 = "";
      String remoteResponse2 = "";
      String totalResponse = "";

      /**
       * UDP Client [mtl, que, she]
       */
      String[] otherTwoServerName = Utils.getOtherTwoServerName(this.getServerName());
      try {
         //Get Inter Server Response 1
         remoteResponse1 = Utils.getRemoteServerQueryResult(Utils.LIST_APP_AVAILABILITY, appointmentType, otherTwoServerName[0]);

         //Get Inter Server Response 2
         remoteResponse2 = Utils.getRemoteServerQueryResult(Utils.LIST_APP_AVAILABILITY, appointmentType, otherTwoServerName[1]);

      } catch (SocketException e) {
         logger.log(Level.SEVERE, "Socket in ReplicaManagers.Dong.server side (ReplicaManagers.Dong.server impl): " + e.getMessage());
      } catch (IOException e) {
         logger.log(Level.SEVERE, "IO in ReplicaManagers.Dong.server side (ReplicaManagers.Dong.server impl): " + e.getMessage());
      }
      totalResponse = getTotalResponse(localResponse, remoteResponse1, remoteResponse2);
      return totalResponse.trim().isEmpty() ? String.format("%s - No records yet.", appointmentType) : String.format("%s - %s.", appointmentType, totalResponse.trim());
   }

   @Override
   public String getAppointmentSchedule(String patientID) {
      String localResponse = getLocalAppSchedule(patientID);
      String remoteResponse1 = "";
      String remoteResponse2 = "";
      String totalResponse = "";

/**
 * UDP Client [mtl, que, she]
 */
      String[] otherTwoServerName = Utils.getOtherTwoServerName(this.getServerName());
      try {
         //Get Inter Server Response 1
         remoteResponse1 = Utils.getRemoteServerQueryResult(Utils.GET_APP_SCHE, patientID, otherTwoServerName[0]);

         //Get Inter Server Response 2
         remoteResponse2 = Utils.getRemoteServerQueryResult(Utils.GET_APP_SCHE, patientID, otherTwoServerName[1]);

      } catch (SocketException e) {
         logger.log(Level.SEVERE, "Socket in ReplicaManagers.Dong.server side (ReplicaManagers.Dong.server impl): " + e.getMessage());
      } catch (IOException e) {
         logger.log(Level.SEVERE, "IO in ReplicaManagers.Dong.server side (ReplicaManagers.Dong.server impl): " + e.getMessage());
      }
      totalResponse = getTotalResponse(localResponse, remoteResponse1, remoteResponse2);
      return totalResponse.trim().length() < 8 ? String.format("%s - No records yet.", patientID) : String.format("%s - %s.", this.getServerName(), totalResponse.trim());
   }

   public String getLocalAppSchedule(String patientID) {
      if (!patientsBookingCount.containsKey(patientID)) {
         return "";
      }
      Map<String, String> appointmentIdRecords = new HashMap<>();

      cityRecords.forEach((appType, appIdMap) -> appIdMap.forEach((appIdString, appDetailList) -> {
         if (appDetailList.contains(patientID)) {
            appointmentIdRecords.put(appType, appIdString);
         }
      }));
      return appointmentIdRecords.toString()
              .replace("=", "<--->")
              .replace("{", " ")
              .replace("}", " ");
   }

}
