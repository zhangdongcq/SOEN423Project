package FrontEnd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import FrontEnd.UDPs.UdpFE2SequencerSender;
import corbasystem.IFrontEndServerPOA;

public class FrontEndServerImpl  extends IFrontEndServerPOA {
   private static final long serialVersionUID = 4077329331765423123L;
   private String frontEndName;
   private Map<String, List<String>> allRequestRecords = new HashMap<>();
   private String currentSequenceId;

   FrontEndServerImpl(String frontEndName) {
      this.frontEndName = frontEndName;
   }

   @Override
   public String initiateServer(String city) {
      return null;
   }

   @Override
   public String requestHandler(String userId, String command, String parameters) {
      //TODO: Retrieve the clean response and route back to client
      //1: Gets client commands -- Done!
      //2: Send the msg to sequencer
      String msgToSend = String.join("|", userId, command, parameters);
      UdpFE2SequencerSender sender = new UdpFE2SequencerSender(msgToSend);
      sender.start();
      if(sender.getResult()) System.out.println("send successfully from fe to sequencer");

      //3: Get response from RMs -- Done in Server

      //4: Clean data to detect replica failures, make sure keep only one response in List
      //TODO: call cleanData() and start notifyRM UDP service

      //5: Return result
//      return allRequestRecords.get(currentSequenceId).get(0);
      return "So far so good! ------ " + msgToSend;
   }

   public synchronized void receiveData(String response){
      //TODO: Write the decoded msg into requestRecords;
      //response format(): “sequenceID;response”
      String[] responseArr = response.split(";");
      currentSequenceId = responseArr[0];
      if(allRequestRecords.get(currentSequenceId)!=null){
         allRequestRecords.get(currentSequenceId).add(responseArr[1]);
      } else{
         allRequestRecords.put(currentSequenceId, Arrays.asList(responseArr[1]));
      }
   }

   public String cleanData(String response){
      //TODO: check the quality of response msg, notify RMs the failure if any
      return null;
   }

}
