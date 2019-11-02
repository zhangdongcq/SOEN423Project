package FrontEnd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import corbasystem.IFrontEndServerPOA;

public class FrontEndServerImpl extends IFrontEndServerPOA {
   private static final long serialVersionUID = 4077329331765423123L;
   private String frontEndName;
   private Map<String, List<String>> allRequestRecords = new HashMap<>();
   private String currentSequenceId;

   FrontEndServerImpl(String frontEndName) { this.frontEndName = frontEndName; }

   @Override
   public String initiateServer(String city) {
      return null;
   }

   @Override
   public String requestHandler(String userId, String command, String parameters) {
      //TODO: Retrieve the clean response and route back to client
      //1: Gets client commands -- Done!
      //2: Send the msg to sequencer and thread is blocked until received response from RM or sequencer.
      String msgToSend = String.join("|", userId, command, parameters);
      try (DatagramSocket aSocket = new DatagramSocket()) {//reference of the original socket
         System.out.println("FE SENDER Started........");
         byte[] message = msgToSend.getBytes(); //message to be passed is stored in byte array

         InetAddress aHost = InetAddress.getByName("localhost"); //Host name is specified and the IP address of server host is calculated using DNS.

         int serverPort = 6789;//agreed upon port
         DatagramPacket request = new DatagramPacket(message, msgToSend.length(), aHost, serverPort);//request packet ready
         aSocket.send(request);//request sent out
         System.out.println("Request message sent from the client is : " + new String(request.getData()));

         byte[] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
         DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

         //Client waits until the reply is received, block here, RMS replies-----------------------------------------------------------------------
         aSocket.receive(reply);//reply received and will populate reply packet now.
         System.out.println("Reply received from the server is: " + new String(reply.getData()));//print reply message after converting it to a string from bytes




      } catch (SocketException e) {
         System.out.println("Socket: " + e.getMessage());
      } catch (IOException e) {
         e.printStackTrace();
         System.out.println("IO: " + e.getMessage());
      }



      //4: Clean data to detect replica failures, make sure keep only one response in List
      //TODO: call cleanData() and start notifyRM UDP service

      //5: Return result
//      return allRequestRecords.get(currentSequenceId).get(0);
      return "So far so good! ------ " + msgToSend;
   }

   public synchronized void receiveData(String response) {
      //TODO: Write the decoded msg into requestRecords;
      //response format(): “sequenceID;response”
      String[] responseArr = response.split(";");
      currentSequenceId = responseArr[0];
      if (allRequestRecords.get(currentSequenceId) != null) {
         allRequestRecords.get(currentSequenceId).add(responseArr[1]);
      } else {
         allRequestRecords.put(currentSequenceId, Arrays.asList(responseArr[1]));
      }
   }

   public String cleanData(String response) {
      //TODO: check the quality of response msg, notify RMs the failure if any
      return null;
   }

}
