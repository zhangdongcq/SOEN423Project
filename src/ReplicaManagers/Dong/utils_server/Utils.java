package ReplicaManagers.Dong.utils_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

   public static final String APP_TYPE_PHYSICIAN = "Physician";
   public static final String APP_TYPE_SURGEON = "Surgeon";
   public static final String APP_TYPE_DENTAL = "Dental";
   public static final String LIST_APP_AVAILABILITY = "listAppointmentAvailability";
   public static final String GET_APP_SCHE = "getAppointmentSchedule";
   public static final String BOOK_REMOTE_APP = "bookAppointment";
   public static final String CANCEL_APP = "cancelAppointment";
   public static final String APP_TYPE = "appointmentType";
   public static final String ONE_TIME = "one";
   public static final String TWO_TIME = "two";
   public static final String MAX_TIME = "three";
   public static final String MTL = "MTL";
   public static final String MTL_L = "mtl";
   public static final String QUE = "QUE";
   public static final String QUE_L = "que";
   public static final String SHE = "SHE";
   public static final String SHE_L = "she";


   public static void initiateServer(Map<String, Map<String, List<String>>> cityRecords, String city, Map<String, Integer> patientsBookingCount, List<String> adminRecords) {
      String patientName1 = city.toUpperCase() + "P1111";
      String patientName2 = city.toUpperCase() + "P2222";
      String patientName3 = city.toUpperCase() + "P3333";
      String specialPatientName1 = city.toUpperCase() + "P4444";
      String specialPatientName2 = city.toUpperCase() + "P5555";
      String specialPatientName3 = city.toUpperCase() + "P6666";

      String adminName1 = city.toUpperCase() + "A1111";
      String adminName2 = city.toUpperCase() + "A2222";
      String adminName3 = city.toUpperCase() + "A3333";

      patientsBookingCount.put(patientName1, 1);
      patientsBookingCount.put(patientName2, 1);
      patientsBookingCount.put(specialPatientName1, 1);
      patientsBookingCount.put(specialPatientName2, 1);
      patientsBookingCount.put(specialPatientName3, 1);

      adminRecords.add(adminName1);
      adminRecords.add(adminName2);
      adminRecords.add(adminName3);

      String appointmentId1 = city.toUpperCase() + "A111010";
      String appointmentId2 = city.toUpperCase() + "E131010";
      String appointmentId3 = city.toUpperCase() + "M121010";

      //Record1
      List<String> appDetail1 = new ArrayList<>();
      appDetail1.add("20");
      appDetail1.add(patientName1);
      appDetail1.add(patientName2);
      Map<String, List<String>> appIDMap = new HashMap<>();
      appIDMap.put(appointmentId1, appDetail1);
      cityRecords.put(Utils.APP_TYPE_PHYSICIAN, appIDMap);

      //Record2
      List<String> appDetail2 = new ArrayList<>();
      appDetail2.add("40");
      appDetail2.add(specialPatientName1);
      appDetail2.add(specialPatientName2);
      appDetail2.add(specialPatientName3);
      appDetail2.add(patientName3);
      Map<String, List<String>> appIDMap2 = new HashMap<>();
      appIDMap2.put(appointmentId2, appDetail2);
      cityRecords.put(Utils.APP_TYPE_PHYSICIAN, appIDMap2);

      //Record3
      List<String> appDetail3 = new ArrayList<>();
      appDetail3.add("50");
      appDetail3.add(specialPatientName1);
      appDetail3.add(specialPatientName2);
      appDetail3.add(specialPatientName3);
      appDetail3.add(patientName3);
      Map<String, List<String>> appIDMap3 = new HashMap<>();
      appIDMap3.put(appointmentId3, appDetail3);
      cityRecords.put(Utils.APP_TYPE_SURGEON, appIDMap3);

   }

   public static boolean isPatientExist(String patientID, List<String> list) {
      return list.stream().anyMatch(s -> s.equals(patientID));
   }

   // This method starts a RMI registry on the local host, if it
   // does not already exists at the specified port number.
   public static String startRegistry(int RMIPortNum)
           throws RemoteException {
      try {
         Registry registry = LocateRegistry.getRegistry(RMIPortNum);
         registry.list();  // This call will throw an exception
         // if the registry does not already exist
      } catch (RemoteException e) {
         // No valid registry at that port.
         Registry registry = LocateRegistry.createRegistry(RMIPortNum);
         return "RMI registry created at port " + RMIPortNum;
      }
      return "start Registry wrong! Should not be here! Util_Server_startRegistry method.";
   } // end startRegistry

   // This method lists the names registered with a Registry object
   public static String listRegistry(String registryURL)
           throws RemoteException, MalformedURLException {
      StringBuilder nameList = new StringBuilder();
      nameList.append("Registry center contains:\n");
      String[] names = Naming.list(registryURL);
      for (String name : names) nameList.append(name).append("\n");
      return nameList.toString();
   } //end listRegistry

   public static String getServerURL(String city) {
      return getServerURL(city, 9999);
   }

   public static String getServerURL(String city, Integer portNumber) {
      return "rmi://localhost:" + portNumber + "/" + city;
   }

   public static String[] getOtherTwoServerName(String localServerName) {
      switch (localServerName.toUpperCase()) {
         case MTL:
         case MTL_L:
            return new String[]{QUE_L, SHE_L};
         case QUE:
         case QUE_L:
            return new String[]{MTL_L, SHE_L};
         default:
            return new String[]{MTL_L, QUE_L};
      }
   }

   public static boolean isValidCityInput(String input) {
      return input.equalsIgnoreCase(MTL)
              || input.equalsIgnoreCase(QUE)
              || input.equalsIgnoreCase(SHE);
   }

   public static Integer getUdpPort(String city) {
      switch (city.toLowerCase()) {
         case "mtl":
            return 5678;
         case "que":
            return 5677;
         case "she":
            return 5676;
         default:
            return 0;
      }
   }

   public static String getRemoteServerQueryResult(String command, String param, String serverName) throws IOException {
      try (DatagramSocket clientSocket = new DatagramSocket()) {
         byte[] toSend = String.join(".", command, param).getBytes();

         //Send1
         int serverPort = Utils.getUdpPort(serverName);
         InetAddress serverName2 = InetAddress.getByName("localhost");
         DatagramPacket requestTarget =
                 new DatagramPacket(toSend, toSend.length, serverName2, serverPort);
         clientSocket.send(requestTarget);

         //Receive1
         byte[] repliedData = new byte[1024];
         DatagramPacket reply = new DatagramPacket(repliedData, repliedData.length);
         clientSocket.receive(reply);
         return new String(repliedData, 0, reply.getLength());
      }
   }

   public static String cleanAndConcatAllResponses(String localResponse, String remoteResponse1, String remoteResponse2) {
      List<String> allResponses = Arrays.asList(
              String.join(",", localResponse, remoteResponse1, remoteResponse2)
                      .split(",")
      );
      allResponses.sort(Comparator.comparing((String o) -> o.substring(0, 5)).thenComparing(o -> o.substring(5)));
//      allResponses.sort(Comparator.naturalOrder());
      return String.join(";", allResponses);
   }

   public static String cleanAndConcatAllResponsesForGetSchedule(String localResponse, String remoteResponse1, String remoteResponse2) {

      List<String> phy = new ArrayList<>();
      List<String> den = new ArrayList<>();
      List<String> sur = new ArrayList<>();
      getAppData(phy, den, sur, localResponse);
      getAppData(phy, den, sur, remoteResponse1);
      getAppData(phy, den, sur, remoteResponse2);
      sortList(phy);
      sortList(den);
      sortList(sur);
      StringBuffer sb = new StringBuffer();
      sb.append(concateList(den, APP_TYPE_DENTAL));
      sb.append(concateList(phy, APP_TYPE_PHYSICIAN));
      sb.append(concateList(sur, APP_TYPE_SURGEON));
      return sb.toString().isEmpty()? "" : sb.toString().substring(1);
   }

   private static void getAppData(List<String> phy, List<String> den, List<String> sur, String response) {

      String[] s1 = response
              .replace("{", "")
              .replace("}", "")
              .trim().split("]"); //pysician=[MTLA121212, MTLE121212    ,dental=[MTLA101010, MTLE1212
      for (String s : s1) {
         if (s.startsWith(",")) s = s.substring(2);
         String[] s2 = s.replace("=[", ",").replace(", ", ",").split(",");
         if (s2[0].contains(APP_TYPE_PHYSICIAN)) {
            for (int i = 1; i < s2.length; ++i) phy.add(APP_TYPE_PHYSICIAN + ";"+s2[i]);
         } else if (s2[0].contains(APP_TYPE_DENTAL)) {
            for (int i = 1; i < s2.length; ++i) den.add(APP_TYPE_DENTAL + ";" +s2[i]);
         } else if (s2[0].contains(APP_TYPE_SURGEON)) {
            for (int i = 1; i < s2.length; ++i) sur.add(APP_TYPE_SURGEON + ";" +s2[i]);
         }
      }
   }

   private static void sortList(List<String> list) {
      if (list.size() == 0) return;
      list.sort(Comparator.comparing((String o) -> o.split(";")[1].substring(0, 5)).thenComparing(o -> o.split(";")[1].substring(5)));
   }

   private static String concateList(List<String> list, String name) {
      String result = "";
      if (list.size() != 0) {
         result = ";" + String.join(";", list);
      }
      return result;
   }


   public static String getRealAppType(String optionNumber) {
      switch (optionNumber) {
         case "1":
            return APP_TYPE_PHYSICIAN;
         case "2":
            return APP_TYPE_SURGEON;
         case "3":
            return APP_TYPE_DENTAL;
         default:
            return "Wrong option number, not 1, 2, 3 for appointment type!";
      }
   }

   public static String getCityFromAppointmentID(String appointmentID) {
      return appointmentID.substring(0, 3);
   }
}
