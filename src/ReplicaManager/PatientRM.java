package ReplicaManager;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PatientRM extends RM implements Runnable{
	static Map<Integer, String> patientMap = new HashMap<>();
	String result="";
	String messageToFE="";
	public PatientRM()
	{
		patientMap.put(1,"MTLP0001");patientMap.put(2,"MTLP0002");patientMap.put(3,"MTLP0003");patientMap.put(4,"MTLP0004");patientMap.put(5,"MTLP0005");	
		patientMap.put(6,"MTLP0006");patientMap.put(7,"MTLP0007");patientMap.put(8,"MTLP0008");patientMap.put(9,"MTLP0009");patientMap.put(10,"MTLP0010");	
		
		patientMap.put(11,"QUEP0001");patientMap.put(12,"QUEP0002");patientMap.put(13,"QUEP0003");patientMap.put(14,"QUEP0004");patientMap.put(15,"QUEP0005");	
		patientMap.put(16,"QUEP0006");patientMap.put(17,"QUEP0007");patientMap.put(18,"QUEP0008");patientMap.put(19,"QUEP0009");patientMap.put(10,"QUEP0020");
		
		patientMap.put(21,"SHEP0001");patientMap.put(22,"SHEP0002");patientMap.put(23,"SHEP0003");patientMap.put(24,"SHEP0004");patientMap.put(25,"SHEP0005");	
		patientMap.put(26,"SHEP0006");patientMap.put(27,"SHEP0007");patientMap.put(28,"SHEP0008");patientMap.put(29,"SHEP0009");patientMap.put(20,"SHEP0010");	

	}
	public Map<Integer, String> getPatientMap()
	{
		return patientMap;
	}
	public void addPatientMap(String clientId)
	{
		int index=patientMap.size()+1;
		patientMap.put(index, clientId);
	}
	public String patientStart(String[] patitions) throws Exception
	{		
		String clientID=patitions[0];
		String functionName=patitions[2];
		String patientID="";
		String appointmentID="";
		String appointmentType="";
		String oldAppointmentID="";
		String oldAppointmentType="";
		String newAppointmentID="";
		String newAppointmentType="";
		if(clientID.contains("MTL"))
		{
			if(functionName.equalsIgnoreCase("book Appointment")) {
				patientID=patitions[1];
				appointmentID=patitions[3];
				appointmentType=patitions[4];
				result=MTLobj.bookAppointment(clientID,patientID, appointmentID, appointmentType);
				String resultStr=(result.equalsIgnoreCase("true"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+resultStr;
				MTLobj.writeTxtClient(clientID,"book Appointment", resultStr);
				MTLobj.writeTxtServerMTL(clientID,patientID,appointmentType,appointmentID,"book Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("get Appointment Schedule")) {
				patientID=patitions[1];
				result=MTLobj.getAppointmentSchedule(patientID);
				String resultStr=(!result.isEmpty())?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+result;
				//System.out.println("Successfully get the appointment for "+patientID);
				MTLobj.writeTxtClient(clientID,"get Appointment Schedule", resultStr);
				MTLobj.writeTxtServerMTL(clientID,patientID,"-","-","get Appointment Schedule", resultStr);
			}else if(functionName.equalsIgnoreCase("cancel Appointment")) {
				patientID=patitions[1];
				appointmentID=patitions[3];
				appointmentType=patitions[4];
				result=MTLobj.cancelAppointment(clientID, patientID,appointmentID, appointmentType);
				String resultStr=(result.equalsIgnoreCase("Successfully cancelled"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+resultStr;
				//System.out.println(result);
				MTLobj.writeTxtClient(clientID,"cancel Appointment", resultStr);
				MTLobj.writeTxtServerMTL(clientID,patientID,"-","-","cancel Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("swap Appointment")) {
				patientID=patitions[1];
				oldAppointmentID=patitions[3];
				oldAppointmentType=patitions[4];
				newAppointmentID=patitions[5];
				newAppointmentType=patitions[6];
				result=MTLobj.swapAppointment(clientID, patientID,oldAppointmentID, oldAppointmentType,newAppointmentID, newAppointmentType);
				String resultStr=(result.equalsIgnoreCase("Successfully swapped"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+resultStr;
				//System.out.println(result);
				MTLobj.writeTxtClient(clientID,"swap Appointment", resultStr);
				MTLobj.writeTxtServerMTL(clientID,patientID,"-","-","swap Appointment", resultStr);
			}
		}
		else if(clientID.contains("QUE"))
		{
			if(functionName.equalsIgnoreCase("book Appointment")) {
				patientID=patitions[1];
				appointmentID=patitions[3];
				appointmentType=patitions[4];
				result=QUEobj.bookAppointment(clientID,patientID, appointmentID, appointmentType);
				String resultStr=(result.equalsIgnoreCase("true"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+resultStr;
				QUEobj.writeTxtClient(clientID,"book Appointment", resultStr);
				QUEobj.writeTxtServerMTL(clientID,patientID,appointmentType,appointmentID,"book Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("get Appointment Schedule")) {
				patientID=patitions[1];
				result=QUEobj.getAppointmentSchedule(patientID);
				String resultStr=(!result.isEmpty())?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+result;
				//System.out.println("Successfully get the appointment for "+patientID);
				QUEobj.writeTxtClient(clientID,"get Appointment Schedule", resultStr);
				QUEobj.writeTxtServerMTL(clientID,patientID,"-","-","get Appointment Schedule", resultStr);
			}else if(functionName.equalsIgnoreCase("cancel Appointment")) {
				patientID=patitions[1];
				appointmentID=patitions[3];
				appointmentType=patitions[4];
				result=QUEobj.cancelAppointment(clientID, patientID,appointmentID, appointmentType);
				String resultStr=(result.equalsIgnoreCase("Successfully cancelled"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+resultStr;
				//System.out.println(result);
				QUEobj.writeTxtClient(clientID,"cancel Appointment", resultStr);
				QUEobj.writeTxtServerMTL(clientID,patientID,"-","-","cancel Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("swap Appointment")) {
				patientID=patitions[1];
				oldAppointmentID=patitions[3];
				oldAppointmentType=patitions[4];
				newAppointmentID=patitions[5];
				newAppointmentType=patitions[6];
				result=QUEobj.swapAppointment(clientID, patientID,oldAppointmentID, oldAppointmentType,newAppointmentID, newAppointmentType);
				String resultStr=(result.equalsIgnoreCase("Successfully swapped"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+resultStr;
				//System.out.println(result);
				QUEobj.writeTxtClient(clientID,"swap Appointment", resultStr);
				QUEobj.writeTxtServerMTL(clientID,patientID,"-","-","swap Appointment", resultStr);
			}
		}
		else if(clientID.contains("SHE"))
		{
			if(functionName.equalsIgnoreCase("book Appointment")) {
				patientID=patitions[1];
				appointmentID=patitions[3];
				appointmentType=patitions[4];
				result=SHEobj.bookAppointment(clientID,patientID, appointmentID, appointmentType);
				String resultStr=(result.equalsIgnoreCase("true"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+resultStr;
				SHEobj.writeTxtClient(clientID,"book Appointment", resultStr);
				SHEobj.writeTxtServerMTL(clientID,patientID,appointmentType,appointmentID,"book Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("get Appointment Schedule")) {
				patientID=patitions[1];
				result=SHEobj.getAppointmentSchedule(patientID);
				String resultStr=(!result.isEmpty())?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+result;
				//System.out.println("Successfully get the appointment for "+patientID);
				SHEobj.writeTxtClient(clientID,"get Appointment Schedule", resultStr);
				SHEobj.writeTxtServerMTL(clientID,patientID,"-","-","get Appointment Schedule", resultStr);
			}else if(functionName.equalsIgnoreCase("cancel Appointment")) {
				patientID=patitions[1];
				appointmentID=patitions[3];
				appointmentType=patitions[4];
				result=SHEobj.cancelAppointment(clientID, patientID,appointmentID, appointmentType);
				String resultStr=(result.equalsIgnoreCase("Successfully cancelled"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+resultStr;
				//System.out.println(result);
				SHEobj.writeTxtClient(clientID,"cancel Appointment", resultStr);
				SHEobj.writeTxtServerMTL(clientID,patientID,"-","-","cancel Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("swap Appointment")) {
				patientID=patitions[1];
				oldAppointmentID=patitions[3];
				oldAppointmentType=patitions[4];
				newAppointmentID=patitions[5];
				newAppointmentType=patitions[6];
				result=SHEobj.swapAppointment(clientID, patientID,oldAppointmentID, oldAppointmentType,newAppointmentID, newAppointmentType);
				String resultStr=(result.equalsIgnoreCase("Successfully swapped"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+resultStr;
				//System.out.println(result);
				SHEobj.writeTxtClient(clientID,"swap Appointment", resultStr);
				SHEobj.writeTxtServerMTL(clientID,patientID,"-","-","swap Appointment", resultStr);
			}
		}
		return messageToFE;
	}
	
	public void outputClientInfo()
	{
		patientMap.entrySet().forEach(entry->{System.out.print(" "+entry.getValue());});
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
