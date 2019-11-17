import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	private static int currentSequenceNum;
	private static int rmID = 2;
	private static HashMap<Integer, String> buffer;
	public static Log logFile;
	
	
	public int getRmID() {return rmID;}
	
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
	            ORB orb = ORB.init(new String[]{"-ORBInitialPort","1050","-ORBInitialHost","localhost"},null);
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
		            	String appointmentID = arg.get(0);
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
			System.out.println(logFile.writeLog("RM"+rmID+" received a message with sequence number greater than current sequence number"));
			return "";
			
		}
		currentSequenceNum++;
		return result;
	}
	
	
	
	
	
	
	
	public static void main(String args[]){
	
		boolean running = true;
		logFile = new Log("RM.txt");
		
		while(running){
			
			String sequencerRequest = SequencerListener.getSequencerRequest();
			// “FE_IP;FE_UPD_Port;SequenceId;UserId;Command;Arguments”   or “RM#;FAIL 
			// “127.0.0.1;8675;1;MTLA2222;addAppointment;appointmentID;appointmentType;capacity”

			List<String> parts = Arrays.asList(sequencerRequest.split(";"));
			
			if(parts.size() == 2 && parts.get(0).equals("RM"+rmID) && parts.get(1).equals("FAIL")){
				running = false;
				System.out.println(logFile.writeLog("RM"+rmID+" received a message ["+parts.get(0)+" "+parts.get(1)+"] from Sequencer"));
				continue;	
			}else if(parts.size() > 2){
				SequencerListener.sendAck("ack");
				System.out.println(logFile.writeLog("RM"+rmID+" sent an ACK to Sequencer "));
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
				String replyToFe = digest(sequenceNum, userID, command, arguments);
				if(replyToFe.equals(""))
					continue;
				else{
					FEreplyer.replyToFe(replyToFe, feIP, fePort);
					System.out.println(logFile.writeLog("RM"+rmID+" sent a reply: ["+replyToFe+"] to FE "+feIP+":"+fePort));
					replyToFe = processBuffer();
					if(replyToFe.equals("")){
						continue;
					}else{
						FEreplyer.replyToFe(replyToFe, feIP, fePort);
						System.out.println(logFile.writeLog("RM"+rmID+" sent a reply: ["+replyToFe+"] to FE "+feIP+":"+fePort));
					}		
				}
			}
		}//end while
	}//end main	
}//end class
