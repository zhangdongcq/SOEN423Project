package FrontEnd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import corbasystem.IFrontEndServerPOA;

public class FrontEndServerImpl extends IFrontEndServerPOA {
   private static final long serialVersionUID = 4077329331765423123L;
   private String frontEndName;
   private Map<String, HashMap<Integer, String>> allRequestRecords = new ConcurrentHashMap<>();
   private String currentSequenceId;
   private String localhost = "localhost";
   private static final int timeOutTwenty = 20;
   //private static final int timeOutTwoHundreds = 2000; 
   private static int rmTimeOut = 6000;//TODO make this dynamic (exponential moving average??)
   private static final int sequencerUdpPort = 6789;
   private static final int frontEndReplicaManagerListenerUdpPort = 7789;
   private static int numberOfRMs;
   private static Set<Integer> replicaNames = new HashSet<Integer>();
   //0: Sequencer. 1: RM1. 2: RM2. 3: RM3. 4: RM4.
   private boolean finalResult = false;
   private boolean allResponsesReceived = false;
   public long timeStampReceiveFromRM;
   //a hashmap to store each RM name and its response time (RTT)
   private HashMap<String, Integer> rtt = new HashMap<String, Integer>();
   public int RTT;
   private static final String noMajorityString = "No majority found, not able to tell you the result.";
   private static final String NO_MAJORITY = "NO_MAJORITY";
   private static final String FAIL = "FAIL";
   private static final String SUCCESS = "SUCCESS";
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
      String result = getCleanResponse();
      if(result.equals(noMajorityString)){
    	  for(int num : replicaNames){
    		  sendFailureMessageToAll(String.valueOf(num));
    	  }
      }
      return getCleanResponse();
   }

   private void sendFailureMessage() {
      int failureMachineId = Utils.findFailureMachine(allRequestRecords.get(currentSequenceId));
      numberOfRMs--;
//      replicaNames.remove(failureMachineId);
      replicaNames.remove(failureMachineId);
      UdpServer failureNoticeUdpThread = new UdpServer(sequencerUdpPort, localhost, failureMachineId + ";FAIL", allRequestRecords, timeOutTwenty);
      failureNoticeUdpThread.start();
   }
   
   private void sendFailureMessageToAll(String failureMachineId) {
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
//        	 System.out.println("RM TIME OUT BEFORE ALL RESPONSES: "+ rmTimeOut);
            String response = getResponseFromRM(aSocket, buffer);
            String[] detailedResponse = response.split(";");
            currentSequenceId = detailedResponse[0];
            addMessageToRecords(detailedResponse);
            allResponsesReceived = Utils.isAllPopulated(allRequestRecords.get(currentSequenceId), numberOfRMs);

            if (allResponsesReceived){ 
//            	//after all responses are received, set a new timeOut
//            	rmTimeOut = setNewTimeOutRm(rtt);
//            	System.out.println("RM TIME OUT AFTER ALL RESPONSES: "+ rmTimeOut);
            	break;
            }
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

   
   public int getRTT(long sent, long received){
	   RTT = (int) (received - sent);
	   return RTT;
   }


   public void updateRTT(String response, int newRTT){
	   String [] parts = response.split(";");
	   String rmID = parts[1];
	   if(!rtt.isEmpty()){
		   for(HashMap.Entry<String, Integer> entry: rtt.entrySet()){
			   if(entry.getKey().equals(rmID)){
				   rtt.replace(entry.getKey(),newRTT);
				   return;
			   }
		   }
		   rtt.put(rmID, newRTT);
	   }
	   else{
		   rtt.put(rmID, newRTT); 
	   }

   }

   //this function finds the largest (longest) time from all Replica Managers and it sets a new TimeOut depending on the logest RTT
   public int setNewTimeOutRm(HashMap<String, Integer> rtt){
	   int newTimeOut=0;
	   if(!rtt.isEmpty()){
		 long longestResponse = rtt.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getValue();
		 newTimeOut = (int) (longestResponse*2);

	   }

	   return newTimeOut;
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
      aSocket.setSoTimeout(rmTimeOut);
      aSocket.receive(reply);
//      timeStampReceiveFromRM = System.currentTimeMillis();
//      RTT = getRTT(UdpServer.timeStampSendRequestToSequencer,timeStampReceiveFromRM); 
      String re = new String(bufferLocal, 0, reply.getLength());
//      updateRTT(re, RTT);//put that RTT in the hashMap with all RTTs for all Replica Managers
      if(Objects.isNull(re)) return "ERROR: EMPTY RESPONSE";
      String replicaNumber = re.split(";")[1];
      System.out.println("Got a response from a Replica Manager " + replicaNumber +"："+re);
      return new String(bufferLocal, 0, reply.getLength());
   }
   
   

   private String getCleanResponse() {
      //4: Clean data to detect replica failures, make sure keep only one response in List
      //TODO: call cleanData()
	   ArrayList<String> cleanResponse = Utils.findMajority(allRequestRecords.get(currentSequenceId));
      //5: Return result
      //TODO: Return clean msg to client stub which shows the msg in client console
      switch (cleanResponse.get(0)) {
         case NO_MAJORITY:
            return noMajorityString;
         case FAIL:
            return "The operation could not be completed, the server refused to execute the request.";
         case SUCCESS:
            return "The operation is completed successfully.";
         default:
            return cleanResponse.get(0);
      }
   }
   
   public static Set<Integer> getRmNames()
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