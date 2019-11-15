package FrontEnd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import corbasystem.IFrontEndServerPOA;

public class FrontEndServerImpl extends IFrontEndServerPOA {
   private static final long serialVersionUID = 4077329331765423123L;
   private String frontEndName;
   private Map<String, String[]> allRequestRecords = new ConcurrentHashMap<>();
   private String currentSequenceId;
   //0: Sequencer. 1: RM1. 2: RM2. 3: RM3. 4: RM4.
   private boolean finalResult = false;
   private boolean isAllReady = false;

   FrontEndServerImpl(String frontEndName) {
      this.frontEndName = frontEndName;
   }

   @Override
   public String initiateServer(String city) {
      return null;
   }

   @Override
   public String requestHandler(String userId, String command, String parameters) {
      //TODO: Gets client commands -- Done in client Stub!

      String msgToSend = String.join(";", userId, command, parameters);
      System.out.println("FE Started........");

      //TODO: send to Sequencer and get ACK msg (thread)
      if (!msgToSend.isEmpty()) {
         UdpServer frontEndToSequencerThread = new UdpServer(6789, "localhost", msgToSend, allRequestRecords, 20);
         frontEndToSequencerThread.start();
      }

      //TODO: Receive MSG from RMs and SE
      DatagramSocket aSocket = null;
      try {
         aSocket = new DatagramSocket(7789);
      } catch (SocketException e) {
         e.printStackTrace();
         return "UDP Socket Problem in FE";
      }
      while (!finalResult) {
         try {
            //TODO: prepare to receive Replica Manager msg. Receiving port: 7789
            byte[] buffer = new byte[1024];//to store the received data, it will be populated by what receive method returns
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.
            aSocket.setSoTimeout(200);
            aSocket.receive(reply);
            String response = new String(buffer, 0, reply.getLength());
            String[] detailedResponse = response.split(";");
            String sequenceId = detailedResponse[0];
            currentSequenceId = sequenceId;
            String replicaMachineId = detailedResponse[1];
            String rmMsg = detailedResponse[2];
            if (allRequestRecords.get(sequenceId) == null) {
               String[] records = new String[5];
               records[Integer.parseInt(replicaMachineId)] = rmMsg;
               allRequestRecords.put(sequenceId, records);
            } else {
               allRequestRecords.get(sequenceId)[Integer.parseInt(replicaMachineId)] = rmMsg;
            }
            isAllReady = Utils.isAllPopulated(allRequestRecords.get(currentSequenceId));
            if (isAllReady) break;
         } catch (SocketTimeoutException e) {
            finalResult = true;
         } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
         } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
         } finally {
            if (aSocket != null) aSocket.close();
         }
      }

      if (!isAllReady) {
         int failureMachineId = Utils.findFailureMachine(allRequestRecords.get(currentSequenceId));
         UdpServer failureNoticeUdpThread = new UdpServer(6789, "localhost", failureMachineId + "_FAILURE_NOTICE", allRequestRecords, 20);
         failureNoticeUdpThread.start();
      }

      //4: Clean data to detect replica failures, make sure keep only one response in List
      //TODO: call cleanData()
      String cleanResponse = Utils.getMajority(allRequestRecords.get(currentSequenceId));

      //5: Return result
      //TODO: Return clean msg to client stub which shows the msg in client console
      switch (cleanResponse) {
         case "NO_MAJORITY":
            return "No majority found, not able to tell you the result.";
         case "FAIL":
            return "The operation could not be completed, the server refused to execute the request.";
         case "SUCCESS":
            return "The operation is completed successfully.";
         default:
            return cleanResponse;
      }
   }
}

class UdpServer extends Thread {
   private static final Logger logger = Logger.getLogger("udp_server");
   private int targetUdpPort;
   private String targetAddress;

   public void setMsgToSend(String msgToSend) {
      this.msgToSend = msgToSend;
   }

   private String msgToSend;
   private Map<String, String[]> allRequestRecords;
   private int timeOut;
   private int counter = 1;
   private boolean resend = false;

   UdpServer(int targetUdpPort, String targetAddress, String msgToSend, Map<String, String[]> allRequestRecords, int timeOut) {
      this.targetUdpPort = targetUdpPort;
      this.targetAddress = targetAddress;
      this.msgToSend = msgToSend;
      this.allRequestRecords = allRequestRecords;
      this.timeOut = timeOut;
   }

   @Override
   public void run() {
      while (counter-- > 0) {
         System.out.println("Request message from the client is : " + msgToSend);
         if (resend) msgToSend += ";RESEND";
         DatagramSocket clientSocket = null;
         DatagramPacket requestTarget = null;
         try {
            clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(timeOut);
            byte[] toSend = msgToSend.getBytes();
            byte[] receivedMsgBuffer = new byte[1024];

            //Send
            InetAddress serverName2 = InetAddress.getByName(targetAddress);
            requestTarget =
                    new DatagramPacket(toSend, toSend.length, serverName2, targetUdpPort);
            clientSocket.send(requestTarget);

            //Receive
            byte[] repliedData = new byte[1024];
            DatagramPacket reply = new DatagramPacket(repliedData, repliedData.length);
            clientSocket.receive(reply);
            String response = new String(receivedMsgBuffer, 0, reply.getLength());
            if (!response.equals("FAILURE_NOTICE_ACK")) {
               String sequenceId = response.split("|")[1];
               String msg = response.split("|")[0];
               String[] eachResponse = new String[5]; // slot 0 : SEQUENCER. slot 1 : RM1. slot 2 : RM2...
               eachResponse[0] = msg;
               if (allRequestRecords.get(sequenceId) == null) {
                  allRequestRecords.put(sequenceId, eachResponse);
               } else {
                  allRequestRecords.get(sequenceId)[0] = msg;
               }
            }
         } catch (SocketTimeoutException e) {
            counter++;
            resend = true;
         } catch (SocketException e) {
            logger.log(Level.SEVERE, "Socket: " + e.getMessage());
         } catch (IOException e) {
            logger.log(Level.SEVERE, "IO: " + e.getMessage());
         } catch (Exception re) {
            logger.log(Level.SEVERE, "Exception in HelloServer.main: " + re);
         } finally {
            if (clientSocket != null) clientSocket.close();
         }
      }
   }
}
