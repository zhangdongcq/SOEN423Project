package Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Sequencer {
   private static int globalSequenceCounter = 0;
   private static Map<Integer, String> msgBackup = new HashMap<>();
   private static Queue<String> allMsgs = new LinkedList<>();

   /***
    * DatagramSocket Port must be different with Multicast Port
    * @param args
    */
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
            msgBackup.put(globalSequenceCounter, msgToRMs);
            allMsgs.offer(msgToRMs);


            System.out.println("Request received from client: " + msgFromFrontEnd);
            System.out.println("Msg sent to RMs is: " + msgToRMs);

            //TODO: MultiCast to RMS
            SequencerUdpMultiCaster multiCaster = new SequencerUdpMultiCaster(allMsgs);
            multiCaster.start();

            //TODO: Send FE the client msg arrived successfully
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

class SequencerUdpMultiCaster extends Thread{
   private Queue<String> allMsgs;
   SequencerUdpMultiCaster(Queue<String> allMsgs){
      this.allMsgs = allMsgs;
   }
   @Override
   public void run(){
      try{
         InetAddress group = InetAddress.getByName("239.0.0.1");
         MulticastSocket multicastSocket = new MulticastSocket(9003);
         multicastSocket.setLoopbackMode(false);//Do not send to itself
         multicastSocket.joinGroup(group);
         while(!allMsgs.isEmpty()){
            String msgToMulticast = allMsgs.poll();
            byte[] msgBuffer = msgToMulticast.getBytes();
            DatagramPacket packetToSend = new DatagramPacket(msgBuffer, msgBuffer.length, group, 9003);
            multicastSocket.send(packetToSend);
         }
      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}

