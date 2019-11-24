package ReplicaManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import OperationsApp.Operations;
import OperationsApp.OperationsHelper;
public class RM {
	static int RMnumber=4;
	static ORB orbMTL=null;
	static ORB orbQUE=null;
	static ORB orbSHE=null;
	Operations MTLobj =null;
	Operations QUEobj =null;
	Operations SHEobj =null;
	static AdminRM admin=null;
	static PatientRM patient=null;
	static int sequencerID=0;
	static int expectedID=1;
	static int sequencerIDInBuffer=100;
	static int countFail=0;
	static String FE_IP="";
	static int FE_UPD_Port=0;
	static ArrayList<String> processBuffer=new ArrayList<String>();
	static String responseFromServers="";
	
	public RM() {
		try {
			//-ORBInitialPort 900 -ORBInitialHost localhost
			org.omg.CORBA.Object objRefMTL = orbMTL.resolve_initial_references("NameService");
			NamingContextExt ncRefMTL = NamingContextExtHelper.narrow(objRefMTL);
			MTLobj = (Operations) OperationsHelper.narrow(ncRefMTL.resolve_str("MTLFunctions"));
			
			org.omg.CORBA.Object objRefQUE = orbQUE.resolve_initial_references("NameService");
			NamingContextExt ncRefQUE = NamingContextExtHelper.narrow(objRefQUE);
			QUEobj = (Operations) OperationsHelper.narrow(ncRefQUE.resolve_str("QUEFunctions"));
			
			org.omg.CORBA.Object objRefSHE = orbSHE.resolve_initial_references("NameService");
			NamingContextExt ncRefSHE = NamingContextExtHelper.narrow(objRefSHE);
			SHEobj = (Operations) OperationsHelper.narrow(ncRefSHE.resolve_str("SHEFunctions"));
		}catch (Exception e) {
			System.out.println("RM exception: " + e);
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		Properties props = new Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "1090");
        props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
		orbMTL = ORB.init(args, props);
		orbQUE = ORB.init(args, props);
		orbSHE = ORB.init(args, props);
		RM rm=new RM();
		admin=new AdminRM();
		patient=new PatientRM();
		try {
			receiveFromSequencer();
//			System.out.println("Sending resposne to FE: " + responseFromServers);
//			sendMessageToFE(responseFromServers);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void receiveFromSequencer() throws Exception {
		
		MulticastSocket aSocket = null;
		try {
			aSocket = new MulticastSocket(6790);
			System.out.println("Replica Manager 1111 Started............");
			aSocket.joinGroup(InetAddress.getByName("228.5.6.9"));
			byte[] buffer = new byte[1000];			
			while (countFail<3) {
				DatagramPacket requestFromSequencer = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(requestFromSequencer);
				
				String sentenceStr = new String(requestFromSequencer.getData());				
				executeRequest(sentenceStr);	
				System.out.println("Sending resposne to FE: " + responseFromServers);
				sendMessageToFE(responseFromServers);
			}
			System.err.println("Failcount exceeds 3, replica has failed!");
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	public static void sendMessageToFE(String message) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] messageByte = message.getBytes();
			InetAddress aHost = InetAddress.getByName(FE_IP);
			
			DatagramPacket request = new DatagramPacket(messageByte, messageByte.length, aHost, FE_UPD_Port);
			aSocket.send(request);
			System.out.println(message);
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}
	public static void executeRequest(String sentenceStr) throws Exception {
		String[] patitions = sentenceStr.split(";");
		patitions = cleanArgumentList(patitions);
		if(patitions.length>2) {
			FE_IP=patitions[0];
			String FE_UPD_PortStr=patitions[1];
			FE_UPD_Port=Integer.parseInt(FE_UPD_PortStr);
			sequencerID=Integer.parseInt(patitions[2]);			
			if(sequencerID==expectedID) {
				if(patitions[3].charAt(3)=='A') {					
					responseFromServers=admin.adminStart(patitions);
				}else {
					responseFromServers=patient.patientStart(patitions);
				}
				expectedID++;
			}else if(sequencerID>expectedID) {
				processBuffer.add(sentenceStr);
			}else if(checkBufferSequencerID(expectedID)) {
				responseFromServers=processInBuffer(expectedID);
			}
		}else if(patitions.length==2) {
			trackFailure(patitions);
		}
	}
	
	 private static String[] cleanArgumentList(String[] requestArgs)
	    {
		 	String[] args = new String[requestArgs.length];
	    	for(int i = 0; i< requestArgs.length; i++)
	    		args[i] = (String)requestArgs[i].trim();
	    	return args;
	    }
	 
	public static boolean checkBufferSequencerID(int expectedID) {
	    String[] bufferedRequest;
		for(int i=0;i<processBuffer.size();i++) {
			bufferedRequest=processBuffer.get(i).split(";");
			if(Integer.parseInt(bufferedRequest[2])==expectedID) {
				return true;
			}
		}
		return false;
	}
	public static String processInBuffer(int expectedID) throws Exception {
		String[] bufferedRequest;
		String returnFromReplica="";
		for(int i=0;i<processBuffer.size();i++) {
			bufferedRequest=processBuffer.get(i).split(";");
			if(Integer.parseInt(bufferedRequest[2])==expectedID) {
				if(bufferedRequest[0].charAt(3)=='A') {
					
					returnFromReplica=admin.adminStart(bufferedRequest);
				}else {
					returnFromReplica=patient.patientStart(bufferedRequest);
				}
				expectedID++;
			}
		}
		return returnFromReplica;
	}
	public static void trackFailure(String[] patitions) {
		if(Integer.parseInt(patitions[0])==RMnumber)
			countFail++;
	}
}
