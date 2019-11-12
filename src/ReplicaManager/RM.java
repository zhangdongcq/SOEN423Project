package ReplicaManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import OperationsApp.Operations;
import OperationsApp.OperationsHelper;
public class RM {
	
	static ORB orbMTL=null;
	static ORB orbQUE=null;
	static ORB orbSHE=null;
	Operations MTLobj =null;
	Operations QUEobj =null;
	Operations SHEobj =null;
	static AdminRM admin=null;
	static PatientRM patient=null;
	static int sequencerID=0;
	static int increaseSeqID=1;
	static String responseFromServers="";
	static String messageToFE="";
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
		orbMTL = ORB.init(args, null);
		orbQUE = ORB.init(args, null);
		orbSHE = ORB.init(args, null);
		RM rm=new RM();
		admin=new AdminRM();
		patient=new PatientRM();
		try {
			receiveFromSequencer();
			sendMessageToFE(messageToFE);
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
			while (sequencerID<increaseSeqID) {
				DatagramPacket requestFromSequencer = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(requestFromSequencer);
				String sentenceStr = new String(requestFromSequencer.getData() );
				String[] patitions = sentenceStr.split(";");
				sequencerID=Integer.parseInt(patitions[0]);
				if(patitions[0].charAt(3)=='A') {
					
					responseFromServers=admin.adminStart(patitions);
				}else {
					responseFromServers=patient.patientStart(patitions);
				}
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}
	public static void checkMsgFromServers(String responseFromServers) throws Exception {
		if(!responseFromServers.contains("error")) {
			increaseSeqID++;
		}
			
	}
	public static void sendMessageToFE(String message) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] messageByte = message.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(messageByte, messageByte.length, aHost, 1234);
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
}
