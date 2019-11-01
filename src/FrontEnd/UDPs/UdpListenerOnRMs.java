package FrontEnd.UDPs;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import FrontEnd.FrontEndServerImpl;
import FrontEnd.Utils;

/***
 * UDP. FE waits for replica managers' responses
 * PORT 9001
 */
public class UdpListenerOnRMs extends Thread {
   private static final Logger logger = Logger.getLogger("fe_udp_server");
   private DatagramSocket serverSocket;
   private FrontEndServerImpl frontEndServer;

   public String getRequestFromRMs() {
      return requestFromRMs;
   }

   public void setRequestFromRMs(String requestFromRMs) {
      this.requestFromRMs = requestFromRMs;
   }

   private String requestFromRMs;

   public UdpListenerOnRMs(FrontEndServerImpl frontEndServer) {
      this.frontEndServer = frontEndServer;
   }
   //TODO: Receive RMs reply and operate the map records in FrontEndServerImpl instance

   @Override
   public void run() {
      while (true) {
         try {
            //Receive Request
            /**
             * FE server use local 9001 to interact with RMs;
             */
            int localFrontEndUdpPort = 9001;
            serverSocket = new DatagramSocket(localFrontEndUdpPort);
            byte[] buffer = new byte[1024];

            DatagramPacket requestPackage = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(requestPackage);
            requestFromRMs = new String(buffer, 0, requestPackage.getLength());
            frontEndServer.receiveData(requestFromRMs);

            serverSocket.close();
         } catch (SocketException e) {
            logger.log(Level.SEVERE, "Socket: " + e.getMessage());
         } catch (IOException e) {
            logger.log(Level.SEVERE, "IO: " + e.getMessage());
         } catch (Exception re) {
            logger.log(Level.SEVERE, "Exception in HelloServer.main: " + re);
         } finally {
            if (serverSocket != null) serverSocket.close();
         }
      }
   }
}
