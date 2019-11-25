import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

import DhmsApp.Dhms;
import DhmsApp.DhmsHelper;

public class ReplicaManager {
	
	private static int udpPort = 6790;
	private static int currentSequenceNum = 1;
	private static int rmID = 3;
	private static HashMap<Integer, String> buffer = new HashMap<Integer, String>();
	public static Log logFile;
	
	
	//public int getRmID() {return rmID;}
	
	
	public static void main(String args[]){
	
		boolean running = true;
		logFile = new Log("RM3.txt");
		int countFail = 0;
		while(countFail < 3 ){
			
			String sequencerRequest = getSequencerRequest();
			List<String> parts = Arrays.asList(sequencerRequest.split(";"));
			
			if(parts.size() == 4 && parts.get(2).equals("RM"+rmID) && parts.get(3).equals("FAIL")){
				countFail++;
				System.out.println(logFile.writeLog("RM"+rmID+" received a message ["+parts.get(0)+" "+parts.get(1)+"] from Sequencer"));
				continue;	
			}else if(parts.size() > 4){
				
				String feIP = parts.get(0);
				int fePort = Integer.valueOf(parts.get(1));
				int sequenceNum = Integer.valueOf(parts.get(2));
				String userID = parts.get(3);
				String command = parts.get(4);
				String arguments = "";
				for(int i=5; i<parts.size(); i++){
					arguments = arguments+parts.get(i)+";";
				}
				System.out.println(logFile.writeLog("RM"+rmID+" received a message ["+sequenceNum+" "+userID+" "+command+" "+arguments+"] from Sequencer"));
				System.out.println(logFile.writeLog("RM"+rmID+": current Sequence Number in RM: "+ currentSequenceNum));
				String replyToFe = digest(sequenceNum, userID, command, arguments);
				System.out.println(logFile.writeLog("RM"+rmID+" REPLY: "+ replyToFe));
				if(replyToFe.isEmpty())
					continue;
				else{
					replyToFe(replyToFe, feIP, fePort);
					System.out.println(logFile.writeLog("RM"+rmID+" sent a reply: ["+replyToFe+"] to FE: "+feIP+":"+fePort));
					replyToFe = processBuffer();
					if(replyToFe.isEmpty()){
						continue;
					}else{
						replyToFe(replyToFe, feIP, fePort);
						System.out.println(logFile.writeLog("RM"+rmID+" sent a reply: ["+replyToFe+"] to FE: "+feIP+":"+fePort));
					}		
				}
			}
		}//end while
	}//end main	
	
	public static String getSequencerRequest(){
		MulticastSocket aSocket = null;
		try{    
			//Create socket and buffer
			aSocket = new MulticastSocket(udpPort); 
            System.out.println("RM3 is listening............");

			aSocket.joinGroup(InetAddress.getByName("228.5.6.9"));
			
			byte[] buffer = new byte[1024];
			DatagramPacket request = new DatagramPacket(buffer, buffer.length);     
			aSocket.receive(request); //listen to request
			String msgFromSeq = (new String(request.getData(),0,request.getLength()));
			System.out.println(logFile.writeLog("RM"+rmID+" gets msg from sequencer: " + msgFromSeq));
			return msgFromSeq;
			
		}catch (SocketException e){
				System.out.println("Socket Server: " + e.getMessage());   
			} catch (IOException e) {
				System.out.println("IO: " + e.getMessage());
			} finally {
				if(aSocket != null) aSocket.close();
			}
		return "";
			
	}
	
	
	
	
	
	public static void replyToFe(String msg, String feIP, int fePort){
			
	        DatagramSocket aSocket = null;
	        try {
	            aSocket = new DatagramSocket();
	            byte[] buffer = msg.getBytes();
	            InetAddress aHost = InetAddress.getByName(feIP);
	            DatagramPacket reply = new DatagramPacket(buffer, buffer.length, aHost, fePort);
	            aSocket.send(reply);
	            
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
	
	
	
	public static String processBuffer(){
		String result = "";
		if(!buffer.isEmpty()){
			for(HashMap.Entry<Integer,String> entry : buffer.entrySet()){
				if(entry.getKey()==currentSequenceNum){
					List<String> parameter = Arrays.asList(entry.getValue().split(";"));
					String id = parameter.get(0);
					String com = parameter.get(1);
					String ar = parameter.get(2);
					result = result + digest(entry.getKey(), id, com, ar);
				}
			}
		}
		
		return result;
	}
	
	
	public static String digest(Integer sequenceNum, String userID, String command, String arguments){
		
		String result = sequenceNum.toString()+";"+rmID+";";
		if(sequenceNum == currentSequenceNum){
			try {
	            ORB orb = ORB.init(new String[]{"-ORBInitialPort","1055","-ORBInitialHost","localhost"},null);
	            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
	            NamingContext ncRef = NamingContextHelper.narrow(objRef);
	
	            NameComponent nc = new NameComponent(userID.substring(0,3),"");
	            NameComponent path[] = {nc};
	            Dhms reference = DhmsHelper.narrow(ncRef.resolve(path));
	            
	            List<String> arg = Arrays.asList(arguments.split(";"));
	            
	            switch(command){
		            case "addAppointment": {
		            	if(userID.substring(3).equals("P"))
		            		return "FAIL";
		            	else{
		            		String appointmentID = arg.get(0);
		            		String appointmentType = arg.get(1);
		            		int capacity = Integer.valueOf(arg.get(2));
		            		result = result + reference.addAppointment(appointmentID, appointmentType, capacity);
		            	}
		            }break;
		            case "removeAppointment": {
		            	if(userID.substring(3).equals("P"))
		            		return "FAIL";
		            	else{
		            		String appointmentID = arg.get(0);
		            		String appointmentType = arg.get(1);
		            		result = result + reference.removeAppointment(appointmentID, appointmentType);
		            	}
		            }break;
		            case "listAppointmentAvailability": {
		            	if(userID.substring(3).equals("P"))
		            		return "FAIL";
		            	else{
		            		String appointmentType = arg.get(0);
		            		result = result + reference.listAppointmentAvailability(appointmentType);
		            	}
		            }break;  
		            case "bookAppointment": {
		            	String patientID = arg.get(0);
		            	String appointmentID = arg.get(1);
	            		String appointmentType = arg.get(2);
	            		result = result + reference.bookAppointment(patientID, appointmentID, appointmentType);
		            }break; 
		            case "getAppointmentSchedule": {
		            	String patientID = arg.get(0);
		            	result = result + reference.getAppointmentSchedule(patientID);	
		            }break;  
		            case "cancelAppointment": {
		            	String patientID = arg.get(0);
		            	String appointmentID = arg.get(1);
		            	result = result + reference.cancelAppointment(patientID, appointmentID);	
		            }break;  
		            case "swapAppointment": {
		            	String patientID = arg.get(0);
		            	String oldAppointmentID = arg.get(1);
		            	String oldAppointmentType = arg.get(2);
		            	String newAppointmentID = arg.get(3);
		            	String newAppointmentType = arg.get(4);
		            	result = result + reference.swapAppointment(patientID, oldAppointmentID, oldAppointmentType, newAppointmentID, newAppointmentType);
		            }break;
		            default: 
		            	result = "FAIL";
	            }// end switch  
			}catch (Exception e){
	            logFile.writeLog("ERROR: "+userID+" unknown error;");
	            System.err.println("ERROR: "+e);
	            e.printStackTrace(System.out);
			}
			
		}else if(sequenceNum < currentSequenceNum){
			System.out.println(logFile.writeLog("RM"+rmID+" received a message with sequence number less than current sequence number"));
			return "";
			
		}else if (sequenceNum > currentSequenceNum){
			// put in buffer
			String request = userID+";"+command+";"+arguments;
			buffer.put(sequenceNum,request);
			System.out.println(logFile.writeLog("RM"+rmID+" Buffer: "+buffer.toString()));
			System.out.println(logFile.writeLog("RM"+rmID+" received a message with sequence number greater than current sequence number"));
			return "";
			
		}
		currentSequenceNum++;
		return result;
	}
}//end class
