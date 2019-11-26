package Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Queue;

public class Utils {

   public static void UDPMulticastServer_SendMsg(int port, String ipAddress, Queue<String> allMsgs) throws IOException {
         DatagramSocket socket = new DatagramSocket();
         InetAddress group = InetAddress.getByName(ipAddress);
         boolean queueContainsMessage = !allMsgs.isEmpty();
         if(queueContainsMessage){
            byte[] msg = allMsgs.poll().getBytes();
            DatagramPacket packet = new DatagramPacket(msg, msg.length,
                    group, port);
            for(int i=0; i< 6; i++){
            	socket.send(packet);
            }
         }
         socket.close();
   }
}
