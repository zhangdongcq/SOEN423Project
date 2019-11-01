package Sequencer;

import Sequencer.UDPs.UdpFE2SequencerReceiver;

public class Sequencer {
   private static String msgFromFrontEnd;
   private static String msgToRMs;
   private static int globalSequenceCounter = 0;

   public static void main(String[] args) {

      //Waiting for the message from front end
      UdpFE2SequencerReceiver receiver = new UdpFE2SequencerReceiver();
      receiver.start();
      msgFromFrontEnd = receiver.getReceivedMessage();
      if(!msgFromFrontEnd.isEmpty()){
         //Send messages to Rms
         msgToRMs = encapsulateRequest(msgFromFrontEnd, receiver.getFeAddress(), receiver.getFePort());

      }
   }
   private static String encapsulateRequest(String msgFromFrontEnd, String feAddress, int fePort){
      return String.join(";", feAddress, fePort+"", ++globalSequenceCounter + "", msgFromFrontEnd);
   }
}
