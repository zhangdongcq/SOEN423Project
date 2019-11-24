package ReplicaManager;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AdminRM extends RM implements Runnable{
	static Map<Integer, String> adminMap = new HashMap<>();
	String result="";
	String messageToFE="";
	public AdminRM()
	{
		adminMap.put(1,"MTLA0001");adminMap.put(2,"QUEA0001");adminMap.put(3,"SHEA0001");
	}
	public Map<Integer, String> getAdminMap()
	{
		return adminMap;
	}
	public void addAdminMap(String id)
	{
		adminMap.put(adminMap.size()+1, id);
	}
	public String adminStart(String[] patitions) throws Exception
	{	
		
		String clientID=patitions[3];
		String functionName=patitions[4];
		String patientID="";
		String appointmentID="";
		String appointmentType="";
		String oldAppointmentID="";
		String oldAppointmentType="";
		String newAppointmentID="";
		String newAppointmentType="";
		if(clientID.contains("MTL"))
		{
			if(functionName.equalsIgnoreCase("bookAppointment")) {
				patientID=patitions[5];
				appointmentID=patitions[6];
				appointmentType=patitions[7];
				result=MTLobj.bookAppointment(clientID,patientID, appointmentID, appointmentType);
				String resultStr=(result.contains("Congratulations"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				MTLobj.writeTxtClient(clientID,"book Appointment", resultStr);
				MTLobj.writeTxtServerMTL(clientID,patientID,appointmentType,appointmentID,"book Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("getAppointmentSchedule")) {
				patientID=patitions[5];
				result=MTLobj.getAppointmentSchedule(patientID);
				if(!result.isEmpty())
					result=result.substring(0, result.length()-1);
				String resultStr=(!result.isEmpty())?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+result;
				//System.out.println("Successfully get the appointment for "+patientID);
				MTLobj.writeTxtClient(clientID,"get Appointment Schedule", resultStr);
				MTLobj.writeTxtServerMTL(clientID,patientID,"-","-","get Appointment Schedule", resultStr);
			}else if(functionName.equalsIgnoreCase("cancelAppointment")) {
				patientID=patitions[5];
				appointmentID=patitions[6];
				//appointmentType=patitions[7];
				
				String result1=MTLobj.cancelAppointment(clientID, patientID,appointmentID, "Surgeon");
				String result2=MTLobj.cancelAppointment(clientID, patientID,appointmentID, "Physician");
				String result3=MTLobj.cancelAppointment(clientID, patientID,appointmentID, "Dental");
				if(result1.contains("Successfully cancelled") ||
						result2.contains("Successfully cancelled") || result3.contains("Successfully cancelled"))
						result = "Successfully cancelled";
				else{
					result = "FAIL";
				}
				
				String resultStr=(result.contains("Successfully cancelled"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				MTLobj.writeTxtClient(clientID,"cancel Appointment", resultStr);
				MTLobj.writeTxtServerMTL(clientID,patientID,"-","-","cancel Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("swapAppointment")) {
				patientID=patitions[5];
				oldAppointmentID=patitions[6];
				oldAppointmentType=patitions[7];
				newAppointmentID=patitions[8];
				newAppointmentType=patitions[9];
				result=MTLobj.swapAppointment(clientID, patientID,oldAppointmentID, oldAppointmentType,newAppointmentID, newAppointmentType);
				String resultStr=(result.contains("Successfully swapped"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				MTLobj.writeTxtClient(clientID,"swap Appointment", resultStr);
				MTLobj.writeTxtServerMTL(clientID,patientID,"-","-","swap Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("addAppointment")) {
				appointmentID=patitions[5];
				appointmentType=patitions[6];
				String appointmentWeekStr=findWeek(appointmentID);
				String capacityStr=patitions[7];
				
				if(MTLobj.checkAppointmentExisted(appointmentID,appointmentType)) {
					return "The appointment you entered exists in MTL Database. Please enter another one.";
				}
				
				result=MTLobj.addAppointment(appointmentID,appointmentType,capacityStr,appointmentWeekStr);
				String resultStr=(MTLobj.checkAppointmentExisted(appointmentID,appointmentType)==true)?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				MTLobj.writeTxtClient(clientID,"add Appointment", resultStr);
				MTLobj.writeTxtServerMTL(clientID,clientID,"-","-","add Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("removeAppointment")) {
				appointmentID=patitions[5];
				appointmentType=patitions[6];
					if(!MTLobj.checkAppointmentExisted(appointmentID,appointmentType)) {
						MTLobj.writeTxtServerMTL(clientID,clientID,"-","-","remove Appointment", "Failed");
						return "The appointment you entered does not exist in MTL Database. Please enter another one.";
					}
						
				result=MTLobj.removeAppointment(appointmentID,appointmentType);
				String resultStr=(result.contains("Successfully removed"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				MTLobj.writeTxtClient(clientID,"remove Appointment", resultStr);
				MTLobj.writeTxtServerMTL(clientID,clientID,"-","-","remove Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("listAppointmentAvailability")) {
				appointmentType=patitions[5];
				result=MTLobj.listAppointmentAvailability(appointmentType);
				if(!result.isEmpty())
					result=result.substring(0, result.length()-1);
				String resultStr=(!result.isEmpty())?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+result;
				//System.out.println(result);
				MTLobj.writeTxtClient(clientID,"list Appointment Availability", resultStr);
				MTLobj.writeTxtServerMTL(clientID,clientID,"-","-","list Appointment Availability", resultStr);
			}
		}
		else if(clientID.contains("QUE"))
		{
			if(functionName.equalsIgnoreCase("bookAppointment")) {
				patientID=patitions[5];
				appointmentID=patitions[6];
				appointmentType=patitions[7];
				result=QUEobj.bookAppointment(clientID,patientID, appointmentID, appointmentType);
				String resultStr=(result.contains("Congratulations"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				QUEobj.writeTxtClient(clientID,"book Appointment", resultStr);
				QUEobj.writeTxtServerMTL(clientID,patientID,appointmentType,appointmentID,"book Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("getAppointmentSchedule")) {
				patientID=patitions[5];
				result=QUEobj.getAppointmentSchedule(patientID);
				if(!result.isEmpty())
					result=result.substring(0, result.length()-1);
				String resultStr=(!result.isEmpty())?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+result;
				//System.out.println("Successfully get the appointment for "+patientID);
				QUEobj.writeTxtClient(clientID,"get Appointment Schedule", resultStr);
				QUEobj.writeTxtServerMTL(clientID,patientID,"-","-","get Appointment Schedule", resultStr);
			}else if(functionName.equalsIgnoreCase("cancelAppointment")) {
				patientID=patitions[5];
				appointmentID=patitions[6];
				//appointmentType=patitions[7];
				
				String result1=MTLobj.cancelAppointment(clientID, patientID,appointmentID, "Surgeon");
				String result2=MTLobj.cancelAppointment(clientID, patientID,appointmentID, "Physician");
				String result3=MTLobj.cancelAppointment(clientID, patientID,appointmentID, "Dental");
				if(result1.contains("Successfully cancelled") ||
						result2.contains("Successfully cancelled") || result3.contains("Successfully cancelled"))
						result = "Successfully cancelled";
				else{
					result = "FAIL";
				}
				String resultStr=(result.contains("Successfully cancelled"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				QUEobj.writeTxtClient(clientID,"cancel Appointment", resultStr);
				QUEobj.writeTxtServerMTL(clientID,patientID,"-","-","cancel Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("swapAppointment")) {
				patientID=patitions[5];
				oldAppointmentID=patitions[6];
				oldAppointmentType=patitions[7];
				newAppointmentID=patitions[8];
				newAppointmentType=patitions[9];
				result=QUEobj.swapAppointment(clientID, patientID,oldAppointmentID, oldAppointmentType,newAppointmentID, newAppointmentType);
				String resultStr=(result.contains("Successfully swapped"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				QUEobj.writeTxtClient(clientID,"swap Appointment", resultStr);
				QUEobj.writeTxtServerMTL(clientID,patientID,"-","-","swap Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("addAppointment")) {
				appointmentID=patitions[5];
				appointmentType=patitions[6];
				String appointmentWeekStr=findWeek(appointmentID);
				String capacityStr=patitions[7];
				
				if(QUEobj.checkAppointmentExisted(appointmentID,appointmentType)) {
					return "The appointment you entered exists in MTL Database. Please enter another one.";
				}
				
				result=QUEobj.addAppointment(appointmentID,appointmentType,capacityStr,appointmentWeekStr);
				String resultStr=(QUEobj.checkAppointmentExisted(appointmentID,appointmentType)==true)?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				QUEobj.writeTxtClient(clientID,"add Appointment", resultStr);
				QUEobj.writeTxtServerMTL(clientID,clientID,"-","-","add Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("removeAppointment")) {
				appointmentID=patitions[5];
				appointmentType=patitions[6];
					if(!QUEobj.checkAppointmentExisted(appointmentID,appointmentType)) {
						QUEobj.writeTxtServerMTL(clientID,clientID,"-","-","remove Appointment", "Failed");
						return "The appointment you entered does not exist in MTL Database. Please enter another one.";
					}
						
				result=QUEobj.removeAppointment(appointmentID,appointmentType);
				String resultStr=(result.contains("Successfully removed"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				QUEobj.writeTxtClient(clientID,"remove Appointment", resultStr);
				QUEobj.writeTxtServerMTL(clientID,clientID,"-","-","remove Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("listAppointmentAvailability")) {
				appointmentType=patitions[5];
				result=QUEobj.listAppointmentAvailability(appointmentType);
				if(!result.isEmpty())
					result=result.substring(0, result.length()-1);
				String resultStr=(!result.isEmpty())?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+result;
				//System.out.println(result);
				QUEobj.writeTxtClient(clientID,"list Appointment Availability", resultStr);
				QUEobj.writeTxtServerMTL(clientID,clientID,"-","-","list Appointment Availability", resultStr);
			}
		}
		else if(clientID.contains("SHE"))
		{
			if(functionName.equalsIgnoreCase("bookAppointment")) {
				patientID=patitions[5];
				appointmentID=patitions[6];
				appointmentType=patitions[7];
				result=SHEobj.bookAppointment(clientID,patientID, appointmentID, appointmentType);
				String resultStr=(result.contains("Congratulations"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				SHEobj.writeTxtClient(clientID,"book Appointment", resultStr);
				SHEobj.writeTxtServerMTL(clientID,patientID,appointmentType,appointmentID,"book Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("getAppointmentSchedule")) {
				patientID=patitions[5];
				result=SHEobj.getAppointmentSchedule(patientID);
				if(!result.isEmpty())
					result=result.substring(0, result.length()-1);
				String resultStr=(!result.isEmpty())?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+result;
				//System.out.println("Successfully get the appointment for "+patientID);
				SHEobj.writeTxtClient(clientID,"get Appointment Schedule", resultStr);
				SHEobj.writeTxtServerMTL(clientID,patientID,"-","-","get Appointment Schedule", resultStr);
			}else if(functionName.equalsIgnoreCase("cancelAppointment")) {
				patientID=patitions[5];
				appointmentID=patitions[6];
				//appointmentType=patitions[7];
				
				String result1=MTLobj.cancelAppointment(clientID, patientID,appointmentID, "Surgeon");
				String result2=MTLobj.cancelAppointment(clientID, patientID,appointmentID, "Physician");
				String result3=MTLobj.cancelAppointment(clientID, patientID,appointmentID, "Dental");
				if(result1.contains("Successfully cancelled") ||
						result2.contains("Successfully cancelled") || result3.contains("Successfully cancelled"))
						result = "Successfully cancelled";
				else{
					result = "FAIL";
				}
				String resultStr=(result.contains("Successfully cancelled"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				SHEobj.writeTxtClient(clientID,"cancel Appointment", resultStr);
				SHEobj.writeTxtServerMTL(clientID,patientID,"-","-","cancel Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("swapAppointment")) {
				patientID=patitions[5];
				oldAppointmentID=patitions[6];
				oldAppointmentType=patitions[7];
				newAppointmentID=patitions[8];
				newAppointmentType=patitions[9];
				result=SHEobj.swapAppointment(clientID, patientID,oldAppointmentID, oldAppointmentType,newAppointmentID, newAppointmentType);
				String resultStr=(result.contains("Successfully swapped"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				SHEobj.writeTxtClient(clientID,"swap Appointment", resultStr);
				SHEobj.writeTxtServerMTL(clientID,patientID,"-","-","swap Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("addAppointment")) {
				appointmentID=patitions[5];
				appointmentType=patitions[6];
				String appointmentWeekStr=findWeek(appointmentID);
				String capacityStr=patitions[7];
				
				if(SHEobj.checkAppointmentExisted(appointmentID,appointmentType)) {
					return "The appointment you entered exists in MTL Database. Please enter another one.";
				}
				
				result=SHEobj.addAppointment(appointmentID,appointmentType,capacityStr,appointmentWeekStr);
				String resultStr=(SHEobj.checkAppointmentExisted(appointmentID,appointmentType)==true)?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				SHEobj.writeTxtClient(clientID,"add Appointment", resultStr);
				SHEobj.writeTxtServerMTL(clientID,clientID,"-","-","add Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("removeAppointment")) {
				appointmentID=patitions[5];
				appointmentType=patitions[6];
					if(!SHEobj.checkAppointmentExisted(appointmentID,appointmentType)) {
						SHEobj.writeTxtServerMTL(clientID,clientID,"-","-","remove Appointment", "Failed");
						return "The appointment you entered does not exist in MTL Database. Please enter another one.";
					}
						
				result=SHEobj.removeAppointment(appointmentID,appointmentType);
				String resultStr=(result.contains("Successfully removed"))?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+resultStr;
				//System.out.println(result);
				SHEobj.writeTxtClient(clientID,"remove Appointment", resultStr);
				SHEobj.writeTxtServerMTL(clientID,clientID,"-","-","remove Appointment", resultStr);
			}else if(functionName.equalsIgnoreCase("listAppointmentAvailability")) {
				appointmentType=patitions[5];
				result=SHEobj.listAppointmentAvailability(appointmentType);
				if(!result.isEmpty())
					result=result.substring(0, result.length()-1);
				String resultStr=(!result.isEmpty())?"SUCCESS":"FAIL";
				messageToFE=sequencerID+";"+"4"+";"+result;
				//System.out.println(result);
				SHEobj.writeTxtClient(clientID,"list Appointment Availability", resultStr);
				SHEobj.writeTxtServerMTL(clientID,clientID,"-","-","list Appointment Availability", resultStr);
			}
		}
		return messageToFE;
	}
	public String findWeek(String appointmentID){
		String dayInStr=appointmentID.substring(4, 6);
		int dayInt=Integer.parseInt(dayInStr);
		String week="";
		if(dayInt>0&&dayInt<8)
			week="1";
		else if(dayInt>7&&dayInt<15)
			week="2";
		else if(dayInt>14&&dayInt<22)
			week="3";
		else if(dayInt>21&&dayInt<32)
			week="4";
		return week;
	}
	
	public void outputClientInfo()
	{
		adminMap.entrySet().forEach(entry->{System.out.print(" "+entry.getValue());});
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
