package FrontEnd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import corbasystem.IFrontEndServerPOA;

public class FrontEndServerImpl extends IFrontEndServerPOA {
   private static final long serialVersionUID = 4077329331765423123L;
   private String frontEndName;
   private Map<String, String[]> allRequestRecords = new HashMap<>();
   private String currentSequenceId;
   //0: Sequencer. 1: RM1. 2: RM2. 3: RM3. 4: RM4.
   private boolean finalResult = false;

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
      //2: Send the msg to sequencer and thread is blocked until received response from RM or sequencer.
      String msgToSend = String.join(";", userId, command, parameters);
      try (DatagramSocket aSocket = new DatagramSocket()) {//reference of the original socket
         System.out.println("FE SENDER Started........");
         byte[] message = msgToSend.getBytes(); //message to be passed is stored in byte array

         InetAddress aHost = InetAddress.getByName("localhost"); //Host name is specified and the IP address of server host is calculated using DNS.
         int serverPort = 6789;//agreed upon port of SEQUENCER
         DatagramPacket request = new DatagramPacket(message, msgToSend.length(), aHost, serverPort);//request packet ready
         long startTime = System.currentTimeMillis();//Timer starts
         aSocket.send(request);//request sent to SEQUENCER
         System.out.println("Request message from the client is : " + new String(request.getData()));


         //TODO: prepare to receive Sequencer ACK msg
         byte[] buffer = new byte[1024];//to store the received data, it will be populated by what receive method returns
         DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.
         long endTime = System.currentTimeMillis();
         //Ensure always a message back
         if (endTime - startTime > 200) return "System idle or failure, as no response received within 200ms";
         aSocket.receive(reply);

         //TODO: Sequencer failed to send back ack msg, FE send again
         String response = new String(buffer, 0, reply.getLength());
         if (endTime - startTime > 20 && !response.startsWith("SEQUENCER_ACK")) {
            System.out.println("No acknowledge sent from sequencer, FE sent the request to SE a second time....");
            request = new DatagramPacket((msgToSend + ";RESEND").getBytes(), (msgToSend + ";RESEND").length(), aHost, serverPort);
            aSocket.send(request); // No ack from sequencer, send again
         }

         //TODO: Receive MSG from RMs and SE
         while (!finalResult) {
            if (response.startsWith("SEQUENCER_ACK")) { // SEQUENCER_ACK|2
               String sequenceId = response.split("|")[1];
               String msg = response.split("|")[0];
               String[] eachResponse = new String[5]; // slot 0 : SEQUENCER. slot 1 : RM1. slot 2 : RM2...
               eachResponse[0] = msg;
               allRequestRecords.put(sequenceId, eachResponse);
               currentSequenceId = sequenceId;
            } else { // “sequenceID;RM#;response”  -> 1;3;SUCCESS
               String[] detailedResponse = response.split(";");
               String sequenceId = detailedResponse[0]; // sequenceID -> 1
               String replicaManagerId = detailedResponse[1]; // RM# -> 3
               String returnMsg = response.substring(sequenceId.length() + replicaManagerId.length() + 2); // SUCCESS
               allRequestRecords.get(sequenceId)[Integer.parseInt(replicaManagerId)] = returnMsg; // RM#;response -> 3;SUCCESS
            }

            if(Arrays.stream(allRequestRecords.get(currentSequenceId)).allMatch(Objects::nonNull)) {
               finalResult = true;
               break;
            }
            aSocket.receive(reply);
            response = new String(buffer, 0, reply.getLength());
         }
      } catch (SocketException e) {
         System.out.println("Socket: " + e.getMessage());
      } catch (IOException e) {
         e.printStackTrace();
         System.out.println("IO: " + e.getMessage());
      }


      //4: Clean data to detect replica failures, make sure keep only one response in List
      //TODO: call cleanData()

      //5: Return result
      //TODO: Return clean msg to client stub which shows the msg in client console
//      return allRequestRecords.get(currentSequenceId).get(0);
      return "So far so good! ------ " + msgToSend;
   }

   private String cleanData(String response) {
      //TODO: check the quality of response msg, notify RMs the failure if any
      return null;
   }

}
