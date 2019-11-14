package FrontEnd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Utils {
   public static final String MTL = "MTL";
   public static final String MTL_L = "mtl";
   public static final String QUE = "QUE";
   public static final String QUE_L = "que";
   public static final String SHE = "SHE";
   public static final String SHE_L = "she";

   private static DatagramSocket serverSocket;
   public static boolean validateRMsResponse(String response){
      //TODO: check whether data is good.
      return true;
   }
   public static boolean UdpSender(String clientIp, int clientPort, String sendMsg){
      boolean sendStatus = false;
      try (DatagramSocket clientSocket = new DatagramSocket()) {
         byte[] toSend = sendMsg.getBytes();

         InetAddress serverName2 = InetAddress.getByName(clientIp);
         DatagramPacket requestTarget =
                 new DatagramPacket(toSend, toSend.length, serverName2, clientPort);
         clientSocket.send(requestTarget);
         sendStatus = true;
      } catch (UnknownHostException e) {
         e.printStackTrace();
         System.out.println("Utils.UdpSender Wrong!");
      } catch (IOException e) {
         e.printStackTrace();
         System.out.println("Utils.UdpSender Wrong!");
      }
      return sendStatus;
   }

   public static DatagramPacket UdpReceiver(int sourcePort){
      String receivedString = "Nothing Received, check Utils.UdpReceived method, line 29";
      DatagramPacket requestPackage;
      byte[] buffer = new byte[1024];
      try {
         serverSocket = new DatagramSocket(sourcePort);
         requestPackage = new DatagramPacket(buffer, buffer.length);
         serverSocket.receive(requestPackage);
         return requestPackage;
      } catch (SocketException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return null;
   }

   public static boolean isValidCityInput(String input) {
      return input.equalsIgnoreCase(MTL)
              || input.equalsIgnoreCase(QUE)
              || input.equalsIgnoreCase(SHE);
   }
}
