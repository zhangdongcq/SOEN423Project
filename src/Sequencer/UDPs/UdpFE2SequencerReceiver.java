package Sequencer.UDPs;

import static FrontEnd.Utils.UdpReceiver;

import java.net.DatagramPacket;
import java.util.logging.Logger;


public class UdpFE2SequencerReceiver extends Thread {
   private static final Logger logger = Logger.getLogger("UdpFE2SequencerReceiver");
   private String receivedMessage;
   private String feAddress;
   private int fePort;

   public UdpFE2SequencerReceiver(){}


   @Override
   public void run() {
      while (true) {
         DatagramPacket requestPackage = UdpReceiver(9001);
         receivedMessage = new String(new byte[1024], 0, requestPackage.getLength());
         feAddress = requestPackage.getAddress().getHostAddress();
         fePort = requestPackage.getPort();
      }
   }

   public String getReceivedMessage() {
      return receivedMessage;
   }

   public void setReceivedMessage(String receivedMessage) {
      this.receivedMessage = receivedMessage;
   }

   public String getFeAddress() {
      return feAddress;
   }

   public int getFePort() {
      return fePort;
   }
}
