package ReplicaManagers.Dong.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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

// Replica Manager
public class Client {
   private static String[] pureRequestArray;
   private static String userType;
   private static String operatorId;
   private static String sequenceId = "1";
   private static String targetCity;
   private static IServer server;
   private static String frontEndIp;
   private static String request;
   private static String frontEndPort;
   private static String replicaManagerId = "1";
   private static Map<String, String[]> requestsFromSequencer = new HashMap<>();
   private static final Logger loggerClient = Logger.getLogger("ReplicaManagers/Dong/client");
   private static final Logger loggerUser = Logger.getLogger("user");

   public static void main(String args[]) {

      //TODO:  127.0.0.1;8675;1;MTLA2222;addAppointment;appointmentID;appointmentType;capacity

      MulticastSocket aSocket = null;
      try {
         aSocket = new MulticastSocket(6790);
         System.out.println("Replica Manager 1111 Started............");
         aSocket.joinGroup(InetAddress.getByName("228.5.6.9"));
         byte[] buffer = new byte[1024];
         while (true) {
            DatagramPacket requestFromSe = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(requestFromSe);

            request = new String(requestFromSe.getData(),
                    requestFromSe.getOffset(), requestFromSe.getLength());
            // 127.0.0.1;8675;1;MTLA2222;addAppointment;appointmentID;appointmentType;capacity
            if (request.contains(";FAIL")) {
               //TODO: Handle three-time failure!
               System.out.println("Got a FAILUIRE!!");
               continue;
            }
            String[] header = request.split(";");
            frontEndIp = header[0];
            frontEndPort = header[1];
            sequenceId = header[2];
            operatorId = header[3];
            targetCity = Utils.getCity(operatorId);
            String cleanRequest = Utils.getPureRequest(request);
            pureRequestArray = cleanRequest.split(";");
            System.out.println(String.join(" | ", pureRequestArray));

            findReplica(args);
            String operationResult = getReplicaResponse().trim();
            Utils.sendResultToFrontEnd(operationResult, sequenceId, replicaManagerId, frontEndIp, frontEndPort);
            System.out.println("(ReplicaManager) gets msg from sequencer: " + request);
            System.out.println("(ReplicaManager) send msg to frontend: " + String.join(";",sequenceId, replicaManagerId, operationResult));
         }
      } catch (SocketException e) {
         System.out.println("Socket: " + e.getMessage());
      } catch (IOException e) {
         System.out.println("IO: " + e.getMessage());
      } catch (CannotProceed cannotProceed) {
         cannotProceed.printStackTrace();
      } catch (NotFound notFound) {
         notFound.printStackTrace();
      } catch (InvalidName invalidName) {
         invalidName.printStackTrace();
      } catch (org.omg.CosNaming.NamingContextPackage.InvalidName invalidName) {
         invalidName.printStackTrace();
      } finally {
         if (aSocket != null)
            aSocket.close();
      }
   }

   private static IServer lookupReplicaServer(String[] args) throws InvalidName, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound {
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
      return server;
   }

   private static String patientActions() throws IOException {
      switch (pureRequestArray[0]) {
         case "bookAppointment": // patientId, appointmentId, appointmentType
            return server.bookAppointment(pureRequestArray[1], pureRequestArray[2], pureRequestArray[3]);

         case "getAppointmentSchedule": // patientID
            return server.getAppointmentSchedule(pureRequestArray[1]);

         case "cancelAppointment":// patientID, String appointmentID
            return server.cancelAppointment(pureRequestArray[1], pureRequestArray[2]);

         case "swapAppointment"://patientId, oldAppointmentId, oldAppointmentType, newAppointmentId, newAppointmentType
            return server.swapAppointment(pureRequestArray[1], pureRequestArray[2], pureRequestArray[3], pureRequestArray[4], pureRequestArray[5]);

         default:
            return "(patientActions - RM) Could not go here, no such a selection in patient menu!";
      }
   }

   private static String adminActions() throws IOException {
      System.out.println("(adminActions) pureRequest: " + String.join("|", pureRequestArray));
      switch (pureRequestArray[0]) {
         case "addAppointment": // appointmentId, realType, capacity
            return server.addAppointment(pureRequestArray[1], pureRequestArray[2], pureRequestArray[3]);

         case "removeAppointment": // appointmentID, appointmentType
            return server.removeAppointment(pureRequestArray[1], pureRequestArray[2]);

         case "listAppointmentAvailability": //appointmentType
            return server.listAppointmentAvailability(pureRequestArray[1]);

         case "bookAppointment": // patientId, appointmentId, appointmentType
            return server.bookAppointment(pureRequestArray[1], pureRequestArray[2], pureRequestArray[3]);

         case "getAppointmentSchedule": // patientID
            return server.getAppointmentSchedule(pureRequestArray[1]);

         case "cancelAppointment":// patientID, String appointmentID
            return server.cancelAppointment(pureRequestArray[1], pureRequestArray[2]);

         case "swapAppointment"://patientId, oldAppointmentId, oldAppointmentType, newAppointmentId, newAppointmentType
            return server.swapAppointment(pureRequestArray[1], pureRequestArray[2], pureRequestArray[3], pureRequestArray[4], pureRequestArray[5]);
         default:
            return "(adminActions - RM) Could not go here, no such a selection in admin menu!";
      }
   }

   static class SequencerListener extends Thread {
      @Override
      public void run() {
         MulticastSocket aSocket = null;
         try {
            aSocket = new MulticastSocket(6790);
            System.out.println("Replica Manager 1111 Started............");
            aSocket.joinGroup(InetAddress.getByName("228.5.6.9"));
            byte[] buffer = new byte[1024];
            while (true) {
               DatagramPacket requestFromSe = new DatagramPacket(buffer, buffer.length);
               aSocket.receive(requestFromSe);

               request = new String(requestFromSe.getData(),
                       requestFromSe.getOffset(), requestFromSe.getLength());
               System.out.println("(ReplicaManager) gets msg from sequencer: " + request);
               putMsg(request);
            }
         } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
         } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
         } finally {
            if (aSocket != null)
               aSocket.close();
         }
      }

      private void putMsg(String request) {
         String[] requestInDetail = request.split(";");
         frontEndIp = requestInDetail[0];
         frontEndPort = requestInDetail[1];
         String seId = requestInDetail[2];
         operatorId = requestInDetail[3];
         String[] reqAndReq = new String[2]; // 0: Request, 1: Response
         //Pure request
         reqAndReq[0] = request.substring(frontEndIp.length() + frontEndPort.length() + sequenceId.length() + 3);
         requestsFromSequencer.put(seId, reqAndReq);
         targetCity = Utils.getCity(operatorId);
         System.out.println(String.format("putMsg: frontEndIp %s, frontEndPort %s, sequenceId %s, requestsFromSequencer %s", frontEndIp, frontEndPort, sequenceId, requestsFromSequencer.toString()));
      }
   }

   private static void findReplica(String[] args) throws InvalidName, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound {
      while (true) {
         if (!targetCity.isEmpty()) {
            server = lookupReplicaServer(args);
            break;
         }
      }
   }

   private static void startSequencerListenerThread() {
      //TODO: GET msg from sequencer; Multicasting listening
      SequencerListener sequencerListenerThread = new SequencerListener();
      sequencerListenerThread.start();
   }

   private static String getReplicaResponse() throws IOException {
      userType = Utils.getUserType(operatorId);
      return userType.equals(Utils.ADMIN) ? adminActions() : patientActions();
   }


}


