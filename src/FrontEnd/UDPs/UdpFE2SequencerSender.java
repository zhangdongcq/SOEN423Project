package FrontEnd.UDPs;

import static FrontEnd.Utils.UdpSender;

import java.util.logging.Logger;

/***
 * UDP. FE collects client's requests
 * and send to sequencer
 */
public class UdpFE2SequencerSender extends Thread {
   private static final Logger logger = Logger.getLogger("UdpFE2SequencerSender");
   private String msgToSend;
   private boolean result = false;
   public UdpFE2SequencerSender(String msgToSend) {
      this.msgToSend = msgToSend;
   }

   @Override
   public void run() {
      while (true) {
         result = UdpSender("localhost", 9002, msgToSend);
      }
   }

   public boolean getResult() {
      return result;
   }

   public void setResult(boolean result) {
      this.result = result;
   }
}
