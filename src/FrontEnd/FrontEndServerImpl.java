package FrontEnd;

import java.util.HashMap;
import java.util.Map;

import corbasystem.IFrontEndServerPOA;

public class FrontEndServerImpl  extends IFrontEndServerPOA {
   private static final long serialVersionUID = 4077329331765423123L;
   private String frontEndName;
   private Map<Integer, String> requestRecords = new HashMap<>();

   FrontEndServerImpl(String frontEndName) {
      this.frontEndName = frontEndName;
   }
   public String getFrontEndName() {
      return frontEndName;
   }

   public void setFrontEndName(String frontEndName) {
      this.frontEndName = frontEndName;
   }

   @Override
   public String initiateServer(String city) {
      return null;
   }

   @Override
   public String requestHandler(String userId, String command, String parameters) {
      //TODO: Retrieve the clean response and route back to client
      return null;
   }

   public void decodeMsg(String response){
      //TODO: Write the decoded msg into requestRecords;
   }

   public void analysizeResponseMsg(String response){
      //TODO: check the quality of response msg, notify RMs the failure if any
   }


}
