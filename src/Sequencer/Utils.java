package Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Queue;

public class Utils {

   public static void MultiCaster(Integer port, String address, Queue<String> allMsgs) {
      try (MulticastSocket multicastSocket = new MulticastSocket(port)) {
         InetAddress group = InetAddress.getByName(address);
         multicastSocket.setLoopbackMode(false);//Do not send to itself
         multicastSocket.joinGroup(group);

         System.out.println("Ready to send to RM");

         if (!allMsgs.isEmpty()) {
            String msgToMulticast = allMsgs.poll();
            byte[] msgBuffer = msgToMulticast.getBytes();
            DatagramPacket packetToSend = new DatagramPacket(msgBuffer, msgBuffer.length, group, 9003);
            multicastSocket.send(packetToSend);
            System.out.println("Done! Sent to RM: " + msgToMulticast);
         }
      } catch (UnknownHostException | SocketException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
         System.out.println("Something wrong in Sequencer: Util.MultiCaster");
      }
   }
}
