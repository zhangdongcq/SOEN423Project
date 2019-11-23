package RM;

import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;
import interaction.CorbaDelivery;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class MessageExecutor {

    private static int RMnumber=2;
    private static String ipAddress = "";
    private static Integer portNumber = 0;
    private static int sequencerID=0;
    private static int expectedID=1;
    private static ArrayList<List<String>> processBuffer=new ArrayList<>();
    private static int countFail = 0;
    private static String response = "";
    private static Pair<String, Integer> ipPort;
    private static boolean hasResponse = false;

    public static void executeRequest(List<String> requestArguments, RemotelyInvokableHospital remotelyInvokableHospital)
    {
    	if(countFail == 3)
    	{
    		System.out.println("This replica has failed (3incorrect responses or timeout). No longer executing requests");
    		return;
    	}
        if(requestArguments.size()>2) {
            ipAddress=requestArguments.get(0);
            String FE_UPD_PortStr=requestArguments.get(1);
            portNumber=Integer.parseInt(FE_UPD_PortStr);
            sequencerID=Integer.parseInt(requestArguments.get(2));
            
            //expectedID = sequencerID; //TODO remove during actual test
            
            if(sequencerID==expectedID) {
                response = CorbaDelivery.deliverCorbaRequest(requestArguments ,remotelyInvokableHospital);
                encapsulateResponse();
                expectedID++;
                hasResponse = true;
            }else if(sequencerID>expectedID) {
                processBuffer.add(requestArguments);
                hasResponse = false;
            }else if(checkBufferSequencerID(expectedID)) {
                response = processInBuffer(expectedID, requestArguments, remotelyInvokableHospital);
                encapsulateResponse();
                hasResponse = true;
            } else {
            	hasResponse = false;
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
    
    private static void encapsulateResponse()
    {
    	if(response.equals("SUCCESS")|| response.equals("FAIL"))
    		response = (expectedID) + ";" + RMnumber +";" + response;
    	else {
    		response = (expectedID) + ";" + RMnumber + response;
    	}
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
    
    public static void setCountFail(int _countFail)
    {
    	countFail = _countFail;
    }

    public static String getResponse()
    {
        return response;
    }
    
    public static boolean hasResponse()
    {
    	return hasResponse;
    }
    
    public static int getRmNumber()
    {
    	return RMnumber;
    }
}
