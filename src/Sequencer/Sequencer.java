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
   private static DatagramPacket request;

   public static void main(String[] args) {
      try (DatagramSocket sequencerServerSocket = new DatagramSocket(6789)) {
         logger.log(Level.INFO, "Sequencer server started, receiving msg from front end............");
         while (true) {// non-terminating loop as the server is always in listening mode.
            String msgFromFrontEnd = recieveMsgFromFE(sequencerServerSocket);
            boolean isMsgNewOne = !msgFromFrontEnd.contains(";RESEND");
            if (isMsgNewOne) {
               multiCastToRMs(msgFromFrontEnd);
            }
            sendAckToFE(msgFromFrontEnd, sequencerServerSocket);
         }
      } catch (SocketException e) {
         logger.log(Level.WARNING, "Socket: " + e.getMessage());
      } catch (IOException e) {
         logger.log(Level.WARNING, "IO: " + e.getMessage());
      }
   }

   private static void sendAckToFE(String msgFromFrontEnd, DatagramSocket sequencerServerSocket) throws IOException
   {
      //TODO: Send acknowledge to FE
      String ack = msgFromFrontEnd.contains("FAILURE_NOTICE") ? "FAILURE_NOTICE_ACK" : "SEQUENCER_ACK|" + (globalSequenceCounter - 1);
      byte[] sequencer_ack_msg = ack.getBytes();
      DatagramPacket reply = new DatagramPacket(sequencer_ack_msg, sequencer_ack_msg.length, request.getAddress(),
              request.getPort());// reply packet ready
      sequencerServerSocket.send(reply);// reply sent
   }

   private static void multiCastToRMs(String msgFromFrontEnd)
   {
      //TODO: Only multicast to Replica Managers if msg is new one
      String feAddress = request.getAddress().getHostAddress();
      int fePort = 7789;
      // Global Counter ++ after encapsulation
      String msgToRMs = encapsulateRequest(msgFromFrontEnd, feAddress, fePort);
      msgBackup.put(globalSequenceCounter - 1, msgToRMs);
      allMsgs.offer(msgToRMs);
      logger.log(Level.INFO, "Request received from client: " + msgFromFrontEnd);
      System.out.println("Msg sent to RMs is: " + msgToRMs);

      MultiCaster(multicastPort, multicastAddress, allMsgs);
      logger.log(Level.INFO, "Message sent to replica managers through Multicaster");
   }

   private static String recieveMsgFromFE(DatagramSocket sequencerServerSocket) throws IOException
   {
      //TODO: Receiving msg from FE
      byte[] buffer = new byte[1024];// to stored the received data from the client.
      request = new DatagramPacket(buffer, buffer.length);
      // Server waits for the request to come, block until a msg is in
      sequencerServerSocket.receive(request);// request received
      return new String(buffer, 0, request.getLength());
   }

   private static String encapsulateRequest(String msgFromFrontEnd, String feAddress, int fePort) {
      return String.join(";", feAddress, fePort + "", ++globalSequenceCounter + "", msgFromFrontEnd);
   }
}
