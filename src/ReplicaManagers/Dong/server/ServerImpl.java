package ReplicaManagers.Dong.server;

import static ReplicaManagers.Dong.utils_server.Utils.getCityFromAppointmentID;
import static ReplicaManagers.Dong.utils_server.Utils.cleanAndConcatAllResponses;

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
         return SUCCESS;
      }
      if (cityRecords.get(appointmentType).containsKey(appointmentID)) {
         return FAIL;
      }
      cityRecords.get(appointmentType).put(appointmentID, appDetail);
      return SUCCESS;
   }

   @Override
   public synchronized String removeAppointment(String appointmentID, String appointmentType) {
      if (appointmentID == null || appointmentType == null || appointmentID.isEmpty() || appointmentType.isEmpty()) {
         return FAIL;
      }
      if (!cityRecords.containsKey(appointmentType)) {
         return FAIL;
      }
      if (!cityRecords.get(appointmentType).containsKey(appointmentID)) {
         return FAIL;
      }
      if (cityRecords.get(appointmentType).get(appointmentID).size() == 1) {
         cityRecords.get(appointmentType).remove(appointmentID);
         return SUCCESS;
      }
      if (cityRecords.get(appointmentType).get(appointmentID).size() > 1) {
         return FAIL;
      }
      return FAIL;
   }

   @Override
   public synchronized String bookAppointment(String patientID, String appointmentID, String appointmentType) {
      if (patientID.isEmpty() || appointmentID.isEmpty() || appointmentType.isEmpty()) {
         return FAIL;
      }
      String numberType = appointmentType;
      appointmentType = Utils.getRealAppType(appointmentType);
      try {
         if (!getCityFromAppointmentID(appointmentID).equalsIgnoreCase(this.serverName)) { // book remote ReplicaManagers.Dong.server app
            return Utils.getRemoteServerQueryResult(Utils.BOOK_REMOTE_APP, String.join(".", patientID.toUpperCase(), appointmentID, numberType), getCityFromAppointmentID(appointmentID).toLowerCase());
         } else {
            if (!cityRecords.containsKey(appointmentType)) {
               return FAIL;
            }
            if (!cityRecords.get(appointmentType).containsKey(appointmentID)) {
               return FAIL;
            }
            List<String> appDetail = cityRecords.get(appointmentType).get(appointmentID);

            if (Integer.parseInt(appDetail.get(0)) == 0) {
               return FAIL;
            }
            appDetail.set(0, String.valueOf(Integer.parseInt(appDetail.get(0)) - 1));
            appDetail.add(patientID.toUpperCase());
            // Records each patient
            patientsBookingCount.put(patientID, patientsBookingCount.getOrDefault(patientID, 1) + 1);
            return SUCCESS;
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      return FAIL;
   }

   @Override
   public synchronized String cancelAppointment(String patientID, String appointmentID) {
      if (patientID.isEmpty() || appointmentID.isEmpty()) {
         return FAIL;
      }
      if (cityRecords.size() == 0) {
         return FAIL;
      }
      // Validate the appointment id
      boolean doesAppointmentIdExist =
              cityRecords.entrySet().stream().anyMatch(entry -> entry.getValue().get(appointmentID) != null);
      if (!doesAppointmentIdExist) {
         return FAIL;
      }

      // Validate the patient id
      boolean doesPatientIdExist =
              cityRecords.entrySet().stream().anyMatch(entry -> entry.getValue().get(appointmentID).contains(patientID));
      if (!doesPatientIdExist) {
         return FAIL;
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
      return SUCCESS;
   }

   @Override
   public String swapAppointment(String patientID, String oldAppointmentID, String oldAppointmentType, String newAppointmentID, String newAppointmentType) {
      String response;
      /**
       * Check local record for given patient ID
       */
      oldAppointmentType = Utils.getRealAppType(oldAppointmentType);
      if (!cityRecords.containsKey(oldAppointmentType))
         return FAIL;
      if (!cityRecords.get(oldAppointmentType).containsKey(oldAppointmentID))
         return FAIL;
      if (!cityRecords.get(oldAppointmentType).get(oldAppointmentID).contains(patientID))
         return FAIL;

      /**
       * UDP Client [mtl, que, she]
       * Check remote Availability
       */
      synchronized (this) {
         response = this.bookAppointment(patientID, newAppointmentID, newAppointmentType);
         if (response.equals(FAIL)) return response;
         this.cancelAppointment(patientID, oldAppointmentID);
         response = SUCCESS;
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
      //Dental;MTLE121236;Dental;SHEA111111;Physician;MTLE121236
      return cleanAndConcatAllResponses(localResponse, remoteResponse1, remoteResponse2);
   }

   @Override
   public String getAppointmentSchedule(String patientID) {
      String localResponse = getLocalAppSchedule(patientID);
      String remoteResponse1 = "";
      String remoteResponse2 = "";

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
      return cleanAndConcatAllResponses(localResponse, remoteResponse1, remoteResponse2);
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
              .replace("=", ";")
              .replace("{", " ")
              .replace("}", " ")
              .trim();
   }
}
