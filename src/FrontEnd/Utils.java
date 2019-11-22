package FrontEnd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Utils {
   public static final String MTL = "MTL";
   public static final String MTL_L = "mtl";
   public static final String QUE = "QUE";
   public static final String QUE_L = "que";
   public static final String SHE = "SHE";
   public static final String SHE_L = "she";

   private static DatagramSocket serverSocket;

   public static boolean validateRMsResponse(String response) {
      //TODO: check whether data is good.
      return true;
   }

   public static boolean UdpSender(String clientIp, int clientPort, String sendMsg) {
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

   public static DatagramPacket UdpReceiver(int sourcePort) {
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

   public static String getMajority(String[] responseList) {
      System.out.println("getMajority ---> HERE");
      int majority;
      if (responseList.length % 2 == 0)
         majority = responseList.length / 2;
      else
         majority = responseList.length / 2 + 1;
      for (int i = 1; i < responseList.length; i++) {
         int numberTheSame = 1;
         for (int j = i + 1; j < responseList.length; j++) {
            if (responseList[i].equals(responseList[j]))
               numberTheSame++;
            if (numberTheSame == majority)
               return responseList[i];
         }
      }
      return "NO_MAJORITY";
   }

   public static String findMajority(String[] responseList) {

      //responseList: [null, SUCCESS, null, null, null ]
      int responseCount = 0;
      for (int i = 1; i < responseList.length; i++) {
         if (!Objects.isNull(responseList[i])) responseCount++;
      }
      if (responseCount == 0) return "No one response received from server, check your networking...";
      Map<String, Integer> map = new HashMap<>();
      for (int i = 1; i < responseList.length; i++) {
         if (!Objects.isNull(responseList[i])) {
            map.put(responseList[i], map.getOrDefault(responseList[i], 0) + 1);
         }
         if (map.get(responseList[i]) > responseCount / 2) return responseList[i];
      }
      return "NO_MAJORITY";

   }

   public static boolean isAllPopulated(String[] responseList) {
      return responseList != null && Arrays.stream(responseList).allMatch(Objects::nonNull);
   }

   public static int findFailureMachine(String[] responseList) {
      for (int i = 0; i < responseList.length; ++i) {
         if (responseList[i] == null) return i;
      }
      return -1;
   }

   public static void notifyOtherRMsTheFaiure(String message, String machineAddress, int udpPort) throws IOException {
      try (DatagramSocket clientSocket = new DatagramSocket()) {
         byte[] toSend = message.getBytes();

         //Send
         int serverPort = udpPort;
         InetAddress serverName2 = InetAddress.getByName(machineAddress);
         DatagramPacket requestTarget =
                 new DatagramPacket(toSend, toSend.length, serverName2, serverPort);
         clientSocket.send(requestTarget);

//         //Receive1
//         byte[] repliedData = new byte[1024];
//         DatagramPacket reply = new DatagramPacket(repliedData, repliedData.length);
//         clientSocket.receive(reply);
//         return new String(repliedData, 0, reply.getLength());
      }
   }
}
