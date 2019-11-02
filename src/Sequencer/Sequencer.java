package Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;

public class Sequencer {
   private static int globalSequenceCounter = 0;
   private static LinkedList<String> allRequests = new LinkedList<>();

   public static void main(String[] args) {
      try (DatagramSocket sequencerServerSocket = new DatagramSocket(6789)) {
         byte[] buffer = new byte[1024];// to stored the received data from
         // the client.
         System.out.println("Sequencer server started, receiving msg from front end............");
         while (true) {// non-terminating loop as the server is always in listening mode.
            //TODO: Receiving msg from FE
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            // Server waits for the request to come, block until a msg is in
            sequencerServerSocket.receive(request);// request received
            String msgFromFrontEnd = new String(buffer, 0, request.getLength());
            String feAddress = request.getAddress().getHostAddress();
            int fePort = request.getPort();
            String msgToRMs = encapsulateRequest(msgFromFrontEnd, feAddress, fePort);
            allRequests.add(msgToRMs);


            System.out.println("Request received from client: " + msgFromFrontEnd);
            System.out.println("Msg sent to RMs is: " + msgToRMs);

            //TODO: MultiCast to RMS
            while (!allRequests.isEmpty()) {
               System.out.println("需要发送给 RM 一个消息：" + allRequests.getFirst());
               allRequests.removeFirst();
            }

            DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(),
                    request.getPort());// reply packet ready

            sequencerServerSocket.send(reply);// reply sent

         }
      } catch (SocketException e) {
         System.out.println("Socket: " + e.getMessage());
      } catch (IOException e) {
         System.out.println("IO: " + e.getMessage());
      }
   }

   private static String encapsulateRequest(String msgFromFrontEnd, String feAddress, int fePort) {
      return String.join(";", feAddress, fePort + "", ++globalSequenceCounter + "", msgFromFrontEnd);
   }
}

class SequencerUdpMultiCaster {

}
