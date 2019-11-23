package FrontEnd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import corbasystem.IFrontEndServerPOA;

public class FrontEndServerImpl extends IFrontEndServerPOA {
   private static final long serialVersionUID = 4077329331765423123L;
   private String frontEndName;
   private Map<String, HashMap<Integer, String>> allRequestRecords = new ConcurrentHashMap<>();
   private String currentSequenceId;
   private String localhost = "localhost";
   private static final int timeOutTwenty = 20;
   private static final int timeOutTwoHundreds = 2000; //TODO make this dynamic (exponential moving average??)
   private static final int sequencerUdpPort = 6789;
   private static final int frontEndReplicaManagerListenerUdpPort = 7789;
   private static int numberOfRMs;
   private static ArrayList<Integer> replicaNames = new ArrayList<>();
   //0: Sequencer. 1: RM1. 2: RM2. 3: RM3. 4: RM4.
   private boolean finalResult = false;
   private boolean allResponsesReceived = false;

   FrontEndServerImpl(String frontEndName) {
      this.frontEndName = frontEndName;
   }

   @Override
   public String initiateServer(String city) {
      return null;
   }

   @Override
   public String requestHandler(String userId, String command, String parameters) {  
      String msgToSend = String.join(";", userId, command, parameters);
      System.out.println("FE Started........");

      //TODO: send to Sequencer and get ACK msg (thread)
      boolean messageExists = !msgToSend.isEmpty();
      if (messageExists) {
         sendReliableAsyncMessageToSequencer(msgToSend);
      }

      //TODO: Receive MSG from RMs

      try(DatagramSocket aSocket = new DatagramSocket(frontEndReplicaManagerListenerUdpPort)) {
         byte[] buffer = new byte[1024];//to store the received data, it will be populated by what receive method returns
         getAllResponseMessagesFromRMs(aSocket, buffer);
         if (!allResponsesReceived) {
            currentSequenceId = String.valueOf(allRequestRecords.size()-1);
            if (Objects.isNull(allRequestRecords.get(currentSequenceId))) return "No any response for your request.";
            sendFailureMessage();
         }
      } catch (SocketException e) {
         e.printStackTrace();
         return "UDP Socket Problem in FE";
      }
      return getCleanResponse();
   }

   private void sendFailureMessage() {
      int failureMachineId = Utils.findFailureMachine(allRequestRecords.get(currentSequenceId));
      UdpServer failureNoticeUdpThread = new UdpServer(sequencerUdpPort, localhost, failureMachineId + ";FAIL", allRequestRecords, timeOutTwenty);
      failureNoticeUdpThread.start();
   }

   private void sendReliableAsyncMessageToSequencer(String msgToSend) {
      UdpServer frontEndToSequencerThread = new UdpServer(sequencerUdpPort, localhost, msgToSend, allRequestRecords, timeOutTwenty);
      frontEndToSequencerThread.start();
   }

   private void getAllResponseMessagesFromRMs(DatagramSocket aSocket, byte[] buffer) {
	   while (!finalResult) {
         try {
            String response = getResponseFromRM(aSocket, buffer);
            String[] detailedResponse = response.split(";");
            currentSequenceId = detailedResponse[0];
            addMessageToRecords(detailedResponse);
            allResponsesReceived = Utils.isAllPopulated(allRequestRecords.get(currentSequenceId), numberOfRMs);

            if (allResponsesReceived) break;
         } catch (SocketTimeoutException e) {
            finalResult = true;
         } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
         } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
         }
      }
//      if (aSocket != null) aSocket.close();
      finalResult = false;
   }

   private void addMessageToRecords(String[] detailedResponse) {
      String replicaMachineId = detailedResponse[1];
      String rmMsg;
      if(detailedResponse.length <3)
    	  rmMsg = "NONE";
      else {
    	  StringBuilder sb  = new StringBuilder();
    	  for(int i =2; i<detailedResponse.length; i++) {
    		  sb.append(detailedResponse[i]);
    		  sb.append(";");
    	  }
    	  rmMsg = sb.toString();
      }

      int replicaNumber = Integer.parseInt(replicaMachineId);
      HashMap<Integer,String> sequenceNumberRecord = allRequestRecords.get(currentSequenceId);
      boolean noRecordExistsForSequenceNumber = Objects.isNull(sequenceNumberRecord);

      if (noRecordExistsForSequenceNumber) {
    	  HashMap<Integer, String> records = new HashMap<>();
    	  for(Integer replica : replicaNames)
    	  {
    		  records.put(replica, null);
    	  }
         //String[] records = new String[numberOfRMs];
         records.put(replicaNumber, rmMsg);//records[replicaNumber] = rmMsg;
         allRequestRecords.put(currentSequenceId, records);
      } else {
         sequenceNumberRecord.put(replicaNumber,rmMsg); 
      }
   }

   private String getResponseFromRM(DatagramSocket aSocket, byte[] buffer) throws SocketException, IOException {
      //TODO: prepare to receive Replica Manager msg. Receiving port: 7789

      byte[] bufferLocal = new byte[1024];
      DatagramPacket reply = new DatagramPacket(bufferLocal, bufferLocal.length);//reply packet ready but not populated.
      aSocket.setSoTimeout(timeOutTwoHundreds);
      aSocket.receive(reply);
      String re = new String(bufferLocal, 0, reply.getLength());
      System.out.println("Got a response from a Replica Manager："+re);
      return new String(bufferLocal, 0, reply.getLength());
   }

   private String getCleanResponse() {
      //4: Clean data to detect replica failures, make sure keep only one response in List
      //TODO: call cleanData()
      String cleanResponse = Utils.findMajority(allRequestRecords.get(currentSequenceId));
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
   
   public static ArrayList<Integer> getRmNames()
   {
	   return replicaNames;
   }
   
   public static void addRM(Integer rmNumber)
   {
	   replicaNames.add(rmNumber);
   }
   
   public static void setNumberOfRMs(int _numberOfRMs)
   {
	   numberOfRMs = _numberOfRMs;
   }
}