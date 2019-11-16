package RM;

import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;
import interaction.CorbaDelivery;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class MessageExecutor {

    private static int RMnumber=4; //TODO add correct RM number
    private static String ipAddress = "";
    private static Integer portNumber = 0;
    private static int sequencerID=0;
    private static int expectedID=0;
    private static ArrayList<List<String>> processBuffer=new ArrayList<>();
    private static int countFail = 0;
    private static String response = "";
    private static Pair<String, Integer> ipPort;

    public static void executeRequest(List<String> requestArguments, RemotelyInvokableHospital remotelyInvokableHospital)
    {
        //TODO make sure request arguments are in the correct order

        if(requestArguments.size()>2) {
            ipAddress=requestArguments.get(0);
            String FE_UPD_PortStr=requestArguments.get(1);
            portNumber=Integer.parseInt(FE_UPD_PortStr);
            sequencerID=Integer.parseInt(requestArguments.get(2));
            if(sequencerID==expectedID) {
                response = CorbaDelivery.deliverCorbaRequest(requestArguments ,remotelyInvokableHospital);
                expectedID++;
            }else if(sequencerID>expectedID) {
                processBuffer.add(requestArguments);
            }else if(checkBufferSequencerID(expectedID)) {
                response=processInBuffer(expectedID, requestArguments, remotelyInvokableHospital);
            }
        }else if(requestArguments.size()==2) {
            trackFailure(requestArguments);
        }
        ipPort = new Pair<>(ipAddress, portNumber);
    }

    private static String processInBuffer(int expectedID, List<String> requestArguments, RemotelyInvokableHospital remotelyInvokableHospital)
    {
        List<String> bufferedRequest;
        String returnFromReplica="";
        for(int i=0;i<processBuffer.size();i++) {
            bufferedRequest=processBuffer.get(i);
            if(Integer.parseInt(bufferedRequest.get(2))==expectedID) {
                response = CorbaDelivery.deliverCorbaRequest(requestArguments ,remotelyInvokableHospital);
                expectedID++;
            }
        }
        return returnFromReplica;
    }

    private static boolean checkBufferSequencerID(int expectedID) {
        List<String> bufferedRequest;
        for(int i=0;i<processBuffer.size();i++) {
            bufferedRequest=processBuffer.get(i);
            if(Integer.parseInt(bufferedRequest.get(2))==expectedID) {
                return true;
            }
        }
        return false;
    }

    private static void trackFailure(List<String> requestArguments) {
        if(Integer.parseInt(requestArguments.get(0))==RMnumber)
            countFail++;
    }

    public static Pair<String, Integer> getIpPort()
    {
        return ipPort;
    }

    public static int getCountFail()
    {
        return countFail;
    }

    public static String getResponse()
    {
        return response;
    }
}
