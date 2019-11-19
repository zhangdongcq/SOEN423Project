package ReplicaManagers.Dong.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

public class Client {
   private static String[] pureRequest;
   private static String userType;
   private static String operatorId;
   private static String sequenceId;
   private static String targetCity;
   private static IServer server;
   private static String frontEndIp;
   private static String frontEndPort;
   private static String request;
   private static Map<String, String[]> requestsFromSequencer;
   private static final Logger loggerClient = Logger.getLogger("ReplicaManagers/Dong/client");
   private static final Logger loggerUser = Logger.getLogger(operatorId);

   public static void main(String args[]) {
      try {

         startSequencerListenerThread();

         //TODO:  127.0.0.1;8675;1;MTLA2222;addAppointment;appointmentID;appointmentType;capacity
         while (true) {
            if (!Objects.isNull(requestsFromSequencer.get(sequenceId)) && !requestsFromSequencer.get(sequenceId)[2].isEmpty()) {

               findReplica(args);

               pureRequest = requestsFromSequencer.get(sequenceId)[0].split(";");

               String operationResult = getReplicaResponse();

               requestsFromSequencer.get(sequenceId)[2] = operationResult;
               loggerUser.log(Level.INFO, operationResult);
               //TODO: UDP send result to FE
               Utils.sendResultToFrontEnd(operationResult, frontEndIp, frontEndPort);
            }
         }
      } catch (InvalidName | IOException | CannotProceed | NotFound | org.omg.CosNaming.NamingContextPackage.InvalidName invalidName) {
         invalidName.printStackTrace();
      }
   } //end main

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
      switch (pureRequest[0]) {
         case "bookAppointment": // patientId, appointmentId, appointmentType
            return server.bookAppointment(pureRequest[1], pureRequest[2], pureRequest[3]);

         case "getAppointmentSchedule": // patientID
            return server.getAppointmentSchedule(pureRequest[1]);

         case "cancelAppointment":// patientID, String appointmentID
            return server.cancelAppointment(pureRequest[1], pureRequest[2]);

         case "swapAppointment"://patientId, oldAppointmentId, oldAppointmentType, newAppointmentId, newAppointmentType
            return server.swapAppointment(pureRequest[1], pureRequest[2], pureRequest[3], pureRequest[4], pureRequest[5]);

         default:
            return "Could not go here, no such a selection in patient menu!";
      }
   }

   private static String adminActions() throws IOException {
      switch (pureRequest[0]) {
         case "addAppointment": // appointmentId, realType, capacity
            return server.addAppointment(pureRequest[1], pureRequest[2], pureRequest[3]);

         case "removeAppointment": // appointmentID, appointmentType
            return server.removeAppointment(pureRequest[1], pureRequest[2]);

         case "listAppointmentAvailability": //appointmentType
            return server.listAppointmentAvailability(pureRequest[1]);

         case "a":
         case "b":
         case "c":
         case "d":
            return patientActions();
         default:
            return "Could not go here, no such a selection in admin menu!";
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

      private synchronized void putMsg(String request) {
         String[] requestInDetail = request.split(";");
         frontEndIp = requestInDetail[0];
         frontEndPort = requestInDetail[1];
         sequenceId = requestInDetail[2];
         operatorId = requestInDetail[3];
         String[] reqAndReq = new String[2]; // 0: Request, 1: Response
         //Pure request
         reqAndReq[0] = request.substring(frontEndIp.length() + frontEndPort.length() + sequenceId.length() + 4);
         requestsFromSequencer = new HashMap<>();
         requestsFromSequencer.put(sequenceId, reqAndReq);
         targetCity = Utils.getCity(operatorId);
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


