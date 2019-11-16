import java.util.HashMap;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

import DhmsApp.Dhms;
import DhmsApp.DhmsHelper;

public class ReplicaManager {
	
	private static int udpPort = 6790;
	private UDPserverRM listener;
	private static int currentSequenceNum;
	private static int rmID = 2;
	private static HashMap<Integer, String> buffer;
	public Log logFile;
	

	public ReplicaManager (){
		
		//create listener
		listener = new UDPserverRM(this, udpPort);
		//start it
		listener.start();
		//create log file
		logFile = new Log ("RM"+rmID+".txt");
		logFile.writeLog("Replica Manager "+rmID+ " is listening for requests from Sequencer.");
		
		
	}
	
	public int getRmID(){
		return rmID;
	}
	
	public String processBuffer(){
		if(!buffer.isEmpty()){
			String result = currentSequenceNum+";"+rmID+";";
			for(HashMap.Entry<Integer,String> entry : buffer.entrySet()){
				if(entry.getKey()==currentSequenceNum){
					String [] param = entry.getValue().split(";");
					String id = param[0];
					String com = param[1];
					String ar = param[2];
					result = result + digest(entry.getKey(), id, com, ar);
				}
			}
			return result;
		}else
			return "dontsend";
		
	}
	
	public String digest(Integer sequenceNum, String userID, String command, String arguments){
		
		String result = sequenceNum.toString()+";"+rmID+";";
		if(sequenceNum == currentSequenceNum){
			try {
	            ORB orb = ORB.init(new String[]{"-ORBInitialPort","1050","-ORBInitialHost","localhost"},null);
	            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
	            NamingContext ncRef = NamingContextHelper.narrow(objRef);
	
	            NameComponent nc = new NameComponent(userID.substring(0,3),"");
	            NameComponent path[] = {nc};
	            Dhms reference = DhmsHelper.narrow(ncRef.resolve(path));
	            
	            String [] arg = arguments.split(";");
	            
	            switch(command){
	            
		            case "addAppointment": {
		            	if(userID.substring(3).equals("P"))
		            		return "FAIL";
		            	else{
		            		String appointmentID = arg[0];
		            		String appointmentType = arg[1];
		            		int capacity = Integer.valueOf(arg[2]);
		            		result = result + reference.addAppointment(appointmentID, appointmentType, capacity);
		            	}
		            }break;
		            
		            case "removeAppointment": {
		            	if(userID.substring(3).equals("P"))
		            		return "FAIL";
		            	else{
		            		String appointmentID = arg[0];
		            		String appointmentType = arg[1];
		            		result = result + reference.removeAppointment(appointmentID, appointmentType);
		            	}
		            }break;
		            
		            case "listAppointmentAvailability": {
		            	if(userID.substring(3).equals("P"))
		            		return "FAIL";
		            	else{
		            		String appointmentType = arg[0];
		            		result = result + reference.listAppointmentAvailability(appointmentType);
		            	}
		            }break;
		            
		            case "bookAppointment": {
		            	String patientID = arg[0];
		            	String appointmentID = arg[1];
	            		String appointmentType = arg[2];
	            		result = result + reference.bookAppointment(patientID, appointmentID, appointmentType);
		            }break;
		            
		            case "getAppointmentSchedule": {
		            	String patientID = arg[0];
		            	result = result + reference.getAppointmentSchedule(patientID);	
		            }break;
		            
		            case "cancelAppointment": {
		            	String patientID = arg[0];
		            	String appointmentID = arg[1];
		            	result = result + reference.cancelAppointment(patientID, appointmentID);	
		            }break;
		            
		            case "swapAppointment": {
		            	String patientID = arg[0];
		            	String oldAppointmentID = arg[1];
		            	String oldAppointmentType = arg[2];
		            	String newAppointmentID = arg[3];
		            	String newAppointmentType = arg[4];
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
			return "dontsend";
			
		}else if (sequenceNum > currentSequenceNum){
			// put in buffer
			String request = userID+";"+command+";"+arguments;
			buffer.put(sequenceNum,request);
			// check buffer for packets to be executed
			
		}
		currentSequenceNum++;
		return result;
	
	
	}//end digest()
	
	public static void main(String args[]){
	
		ReplicaManager rm = new ReplicaManager();
	}
	
		
}
