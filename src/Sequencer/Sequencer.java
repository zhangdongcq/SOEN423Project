package Sequencer;

import static Sequencer.Utils.MultiCaster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sequencer {
   private static final Logger logger = Logger.getLogger("Sequencer");

   private static Map<Integer, String> msgBackup = new HashMap<>();
   private static Queue<String> allMsgs = new LinkedList<>();
   private static Integer globalSequenceCounter = 0;
   private final static String multicastAddress = "228.5.6.9";
   private final static int multicastPort = 6790;

   public static void main(String[] args) {
      try (DatagramSocket sequencerServerSocket = new DatagramSocket(6789)) {
         byte[] buffer = new byte[1024];// to stored the received data from
         // the client.
         logger.log(Level.INFO, "Sequencer server started, receiving msg from front end............");
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

            logger.log(Level.INFO, "Request received from client: " + msgFromFrontEnd);
            System.out.println("Msg sent to RMs is: " + msgToRMs);

            //TODO: Multicast to Replica Managers

            MultiCaster(multicastPort, multicastAddress, allMsgs);
            logger.log(Level.INFO, "Message sent to replica managers through Multicaster");

            //TODO: Send FE the client msg arrived successfully
            DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(),
                    request.getPort());// reply packet ready
            sequencerServerSocket.send(reply);// reply sent
         }
      } catch (SocketException e) {
         logger.log(Level.WARNING, "Socket: " + e.getMessage());
      } catch (IOException e) {
         logger.log(Level.WARNING, "IO: " + e.getMessage());
      }
   }

   private static String encapsulateRequest(String msgFromFrontEnd, String feAddress, int fePort) {
      return String.join(";", feAddress, fePort + "", ++globalSequenceCounter + "", msgFromFrontEnd);
   }
}
