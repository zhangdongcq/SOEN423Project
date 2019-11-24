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
   private static int rmTimeOut = 6000; //TODO make this dynamic (exponential moving average??)
   private static final int sequencerUdpPort = 6789;
   private static final int frontEndReplicaManagerListenerUdpPort = 7789;
   private static int numberOfRMs;
   private static ArrayList<Integer> replicaNames = new ArrayList<>();
   //0: Sequencer. 1: RM1. 2: RM2. 3: RM3. 4: RM4.
   private boolean finalResult = false;
   private boolean allResponsesReceived = false;
   public long timeStampReceiveFromRM;
   //a hashmap to store each RM name and its response time (RTT)
   private HashMap<String, Integer> rtt = new HashMap<String, Integer>();
   //hashmap <RMid, <commnad, numberOfFails>>
   private HashMap <String, HashMap<String, Integer>>  failureRecords = new HashMap <String, HashMap<String, Integer>>();
   public int RTT;

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
        	System.out.println("RM TIME OUT BEFORE ALL RESPONSES: "+ rmTimeOut);
            String response = getResponseFromRM(aSocket, buffer);
            String[] detailedResponse = response.split(";");
            currentSequenceId = detailedResponse[0];
            String rmID = detailedResponse[1];
            String answer = detailedResponse[2];
            String command = UdpServer.command;
//            if(answer.equals("FAIL"))
//            	addToFailureRecords(rmID, command, answer);
//            	//check if there is a failed rm
//            else
            addMessageToRecords(detailedResponse);
            allResponsesReceived = Utils.isAllPopulated(allRequestRecords.get(currentSequenceId), numberOfRMs);

            if (allResponsesReceived){ 
            	//after all responses are received, set a new timeOut
            	rmTimeOut = setNewTimeOutRm(rtt);
            	System.out.println("RM TIME OUT AFTER ALL RESPONSES: "+ rmTimeOut);
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
   
   //there migh be more than one faulty RMs, so return a String containing all faulty Rm names
   public String findFaultyRMs (){
	   String faultyRMs = "";
	   if(!failureRecords.isEmpty()){
		   for(HashMap.Entry<String, HashMap<String, Integer>> entry1: failureRecords.entrySet()){
				   for(HashMap.Entry<String,Integer> entry2: entry1.getValue().entrySet()){
					   if(entry2.getValue() == 3){
						   faultyRMs = faultyRMs + entry1.getKey()+";";
					   }
					   
				   }
		   }
	   }
	return faultyRMs;
   }
   
   
   public void addToFailureRecords(String rmID, String command, String ans){
	   if(!failureRecords.isEmpty()){
		   for(HashMap.Entry<String, HashMap<String, Integer>> entry1: failureRecords.entrySet()){
			   if(entry1.getKey().equals(rmID)){
				   for(HashMap.Entry<String,Integer> entry2: entry1.getValue().entrySet()){
						if(entry2.getKey().equals(command)){
							entry1.getValue().replace(entry2.getKey(), entry2.getValue()+1);
							return;
						}
					}
				   	HashMap<String, Integer> inner = new HashMap<String, Integer>();
				   	inner.put(command, 1);
				   	failureRecords.put(entry1.getKey(), inner);
				   	return;
				}
		    }
		   	HashMap<String, Integer> inner = new HashMap<String, Integer>();
		   	inner.put(command, 1);
		   	failureRecords.put(rmID, inner);
		   	return;
	   }else{
		   //if the hashMap is totally empty, add the first record
		   	HashMap<String, Integer> inner = new HashMap<String, Integer>();
	   		inner.put(command, 1);
	   		failureRecords.put(rmID, inner);
	   }
		   
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
   
   
   
   private String getResponseFromRM(DatagramSocket aSocket, byte[] buffer) throws SocketException, IOException {
      //TODO: prepare to receive Replica Manager msg. Receiving port: 7789

      byte[] bufferLocal = new byte[1024];
      DatagramPacket reply = new DatagramPacket(bufferLocal, bufferLocal.length);//reply packet ready but not populated.
      aSocket.setSoTimeout(rmTimeOut);
      aSocket.receive(reply);
      
      timeStampReceiveFromRM = System.currentTimeMillis();
      RTT = getRTT(UdpServer.timeStampSendRequestToSequencer,timeStampReceiveFromRM); //get individual RTT for the specific Replica Manager
      String re = new String(bufferLocal, 0, reply.getLength());
      updateRTT(re, RTT);//put that RTT in the hashMap with all RTTs for all Replica Managers
      
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