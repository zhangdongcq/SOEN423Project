
import DhmsApp.*;


import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.text.html.HTMLDocument.Iterator;

import java.util.Map;
import java.util.Map.Entry;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DhmsServant extends DhmsPOA{
	private ORB orb;
	public String serverLocation;
	public String hostName;
	public String portNumber;
	//hosts for the 3 servers
	public static String mtlHost, queHost, sheHost = "localhost";
	//datastructure to store appointment records
	private HashMap <String, HashMap<String, Integer>> appointmentRecords = new HashMap<String,HashMap<String,Integer>>();
	//store patient's appointments
	private HashMap<String, HashMap<String, ArrayList<String>>> patientRecords = new  HashMap<String, HashMap<String, ArrayList<String>>>();
	//store admin logs
	private byte [] adminLogin = new byte [10000];
	//store patient logs
	private byte [] patientLogin = new byte [10000];
	//udp ports
	private static int udpPortMtl = 2234;
	private static int udpPortQue = 3331;
	private static int udpPortShe = 3332;
	//udp used for communication between servers
	private UDPserver udpServer;
	//a log file
	public Log logFile;
	
	
	
	public DhmsServant(String hostName, String portNumber, String location){
		this.serverLocation = location;
		this.hostName = hostName;
		this.portNumber = portNumber;
		
		logFile = new Log(serverLocation+"server.txt");
		
		if (serverLocation.equals("MTL")){
            udpServer = new UDPserver(this, udpPortMtl);
            
        }
        else if (serverLocation.equals("QUE")){
            udpServer = new UDPserver(this,udpPortQue);
        }
        else {
            udpServer = new UDPserver(this,udpPortShe);
        }
        //Start the UDP server
		udpServer.start();
		
		logFile.writeLog(serverLocation+" UDP port: "+udpServer.UDPport);
	}
	
	

	

	
	@Override
	public synchronized String addAppointment(String appointmentID, String appointmentType, int capacity) {
		
		if(validateAppointmentID(appointmentID) && validateAppointmentType(appointmentType)){
			if(serverLocation.equals(appointmentID.substring(0,3))){
				if(!appointmentRecords.isEmpty()){
					for(HashMap.Entry<String, HashMap<String, Integer>> entry1 : appointmentRecords.entrySet()){
						if(entry1.getKey().equals(appointmentType)){
							for(HashMap.Entry<String,Integer> entry2: entry1.getValue().entrySet()){
								if(entry2.getKey().equals(appointmentID)){
									logFile.writeLog("This appointment already exists in appointment records"+appointmentID);
									return "FAIL";
								}
								
							}
						
							entry1.getValue().put(appointmentID, capacity);
							logFile.writeLog("Appointment added "+appointmentType+" "+appointmentID+" "+capacity);
							return "SUCCESS";
						}
					}
					HashMap<String, Integer> InnerMap = new HashMap<String, Integer>();
					InnerMap.put(appointmentID, capacity);
					appointmentRecords.put(appointmentType, InnerMap);
					logFile.writeLog("Appointment added "+appointmentType+" "+appointmentID+" "+capacity);
					return "SUCCESS";
	
				}else{
					HashMap<String, Integer> InnerMap = new HashMap<String, Integer>();
					InnerMap.put(appointmentID, capacity);
					appointmentRecords.put(appointmentType, InnerMap);
					logFile.writeLog("Appointment added: "+appointmentType+" "+appointmentID+" "+capacity);
					return "SUCCESS";
				}
			}else{
				logFile.writeLog("addAppointment: Adding to other servers is not permited");
				return "FAIL";
			}
		}else{
			logFile.writeLog("addAppointment: Invalid inputs");
			return "FAIL";
		}
	}
	
	
	
	
	
	

	@Override
	public synchronized String removeAppointment(String appointmentID, String appointmentType) {
		
		if(validateAppointmentID(appointmentID) && validateAppointmentType(appointmentType)){
			if(serverLocation.equals(appointmentID.substring(0,3))){
				if(!appointmentRecords.isEmpty()){
					for(HashMap.Entry<String, HashMap<String, Integer>> entry1 : appointmentRecords.entrySet()){
						for(HashMap.Entry<String,Integer> entry2: entry1.getValue().entrySet()){
							if(entry1.getKey().equals(appointmentType) && entry2.getKey().equals(appointmentID)){
								entry1.getValue().remove(appointmentID);
								if(entry1.getValue().isEmpty())
									appointmentRecords.remove(entry1);
								logFile.writeLog("Appointment removed:"+ appointmentType+ " "+ appointmentID);
								return "SUCCESS";
							}
						}
					}
				}else{
					logFile.writeLog("Attempt to remove appointment from empty records");
					return "FAIL";
				}
			}else
				return "FAIL";
		}else{
			logFile.writeLog("Remove appointment: Inputs are invalid");
			return "FAIL";
		}
		
		logFile.writeLog("Removing was not successful");
		return "FAIL";
	}

	
	
	
	
	@Override
	public String listAppointmentAvailability(String appointmentType) {
		
		if(validateAppointmentType(appointmentType)){
			 String resultStr = "";
			 String request = "list"+appointmentType;
		        UDPclient th1,th2;	
		        
		        
		        if (serverLocation.equals("MTL")){
		            th1 = new UDPclient(serverLocation,"QUE",queHost,udpPortQue,request);
		            th2 = new UDPclient(serverLocation,"SHE",sheHost,udpPortShe,request);
		            logFile.writeLog("["+serverLocation+" Server]: send listAppointmentAvailability("+appointmentType+") request to QUE and SHE server.");
		        }
		        else if(serverLocation.equals("QUE")){
		        	 th1 = new UDPclient(serverLocation,"MTL",mtlHost,udpPortMtl,request);
			         th2 = new UDPclient(serverLocation,"SHE",sheHost,udpPortShe,request);
			         logFile.writeLog("["+serverLocation+" Server]: send listAppointmentAvailability("+appointmentType+") request to MTL and SHE server.");
		        }
		        else{
		        	 th1 = new UDPclient(serverLocation,"MTL",mtlHost,udpPortMtl,request);
			         th2 = new UDPclient(serverLocation,"QUE",queHost,udpPortQue,request);
			         logFile.writeLog("["+serverLocation+" Server]: send listAppointmentAvailability("+appointmentType+") request to MTL and QUE server.");
		        }
	
		        //Start the two threads
		        th1.start();
		        th2.start();
	
		        try {
		            //wait for the first thread end
		            th1.join();
		            logFile.writeLog("["+serverLocation+" Server]: Got the reply of listAppointmentAvailability("+appointmentType+") request from "+th1.remoteServer+" server. \""+th1.replyFromServer+"\"");
	
		            //wait for the second thread end
		            th2.join();
		            logFile.writeLog("["+serverLocation+" Server]: Got the reply of listAppointmentAvailability("+appointmentType+") request from "+th2.remoteServer+" server. \""+th2.replyFromServer+"\"");
	
		            //create return message
		            //resultStr = String.valueOf(listLocalAppointments(appointmentType))+ th1.replyFromServer + th2.replyFromServer;
		            resultStr = removeLastChar(sortListAvailability(String.valueOf(listLocalAppointments(appointmentType))+ th1.replyFromServer + th2.replyFromServer));
	
		            //release thread object
		            th1 = null;
		            th2 = null;
		            
		            logFile.writeLog("["+serverLocation+" Server] " +appointmentType +": "+resultStr);
		            return resultStr;
		        }catch (InterruptedException e)	{
		            e.printStackTrace();
		            logFile.writeLog("["+serverLocation+" Server] ERROR: listing appointments failed, Internal error.");
		            return "FAIL";
		        }
		}else{
			logFile.writeLog("listAppointmentAvailability: Invalid inputs");
			return "FAIL";
		}
	}
	
	
	
	

	
	@Override
	public String bookAppointment(String patientID, String appointmentID, String appointmentType){
		if(validateAppointmentID(appointmentID) && validateAppointmentType(appointmentType) && validatePatientID(patientID)){
			String result = "";
			UDPclient book=null;
			String request = "book"+patientID+appointmentID+appointmentType;
			
			if(appointmentID.substring(0,3).equals(serverLocation)){
				result = bookLocalAppointment(patientID, appointmentID, appointmentType);
				return result;
			}
			
			else if(appointmentID.substring(0, 3).equals("MTL")){
				book = new UDPclient(serverLocation,"MTL",mtlHost,udpPortMtl,request);
				logFile.writeLog("["+serverLocation+" Server]: send bookAppointment() request to MTL server.");
			}
			else if(appointmentID.substring(0, 3).equals("QUE")){
				book = new UDPclient(serverLocation,"QUE",queHost,udpPortQue,request);
				logFile.writeLog("["+serverLocation+" Server]: send bookAppointment() request to QUE server.");
			}
			else if(appointmentID.substring(0, 3).equals("SHE")){
				book = new UDPclient(serverLocation,"SHE",sheHost,udpPortShe,request);
				logFile.writeLog("["+serverLocation+" Server]: send bookAppointment() request to SHE server.");
			}
			
			book.start();
			
			try {
	           book.join();
	           logFile.writeLog("["+serverLocation+" Server]: Got the reply of bookAppointment() request from "+book.remoteServer+" server. \""+book.replyFromServer+"\"");
	           //create return message
	           result = book.replyFromServer;
	           //release thread object
	           book = null;
	           return result;
	        }catch (InterruptedException e)	{
	            e.printStackTrace();
	            logFile.writeLog("["+serverLocation+" Server] ERROR: booking failed, Internal error.");
	            return "FAIL";
	        }
		}else{
			logFile.writeLog("Booking faild, inputs invalid");
			return "FAIL";
		}
	}
	
	


	@Override
	public String getAppointmentSchedule(String patientID) {
		
		if(validatePatientID(patientID)){
			String resultStr = "";
			String request = "sche"+patientID;
		    UDPclient th1,th2;	
		       
	        if (serverLocation.equals("MTL")){
	            th1 = new UDPclient(serverLocation,"QUE",queHost,udpPortQue,request);
	            th2 = new UDPclient(serverLocation,"SHE",sheHost,udpPortShe,request);
	            logFile.writeLog("["+serverLocation+" Server]: send getAppointmentSchedule("+patientID+") request to QUE and SHE server.");
	        }
	        else if(serverLocation.equals("QUE")){
	        	 th1 = new UDPclient(serverLocation,"MTL",mtlHost,udpPortMtl,request);
		         th2 = new UDPclient(serverLocation,"SHE",sheHost,udpPortShe,request);
		         logFile.writeLog("["+serverLocation+" Server]: send getAppointmentSchedule("+patientID+") request to MTL and SHE server.");
	        }
	        else{
	        	 th1 = new UDPclient(serverLocation,"MTL",mtlHost,udpPortMtl,request);
		         th2 = new UDPclient(serverLocation,"QUE",queHost,udpPortQue,request);
		         logFile.writeLog("["+serverLocation+" Server]: send getAppointmentSchedule("+patientID+") request to MTL and QUE server.");
	        }
	
	        //Start the two threads
	        th1.start();
	        th2.start();
	
	        try {
	            //wait for the first thread end
	            th1.join();
	            logFile.writeLog("["+serverLocation+" Server]: Got the reply of getAppointmentSchedule("+patientID+") request from "+th1.remoteServer+" server. \""+th1.replyFromServer+"\"");
	
	            //wait for the second thread end
	            th2.join();
	            logFile.writeLog("["+serverLocation+" Server]: Got the reply of getAppointmentSchedule("+patientID+") request from "+th2.remoteServer+" server. \""+th2.replyFromServer+"\"");
	
	            
	            resultStr = removeLastChar(sortAppointmentSchedule(String.valueOf(getLocalSchedule(patientID))+th1.replyFromServer+th2.replyFromServer));
	            //resultStr = sortAppointmentSchedule(String.valueOf(getLocalSchedule(patientID))+th1.replyFromServer+th2.replyFromServer);

	            //release thread object
	            th1 = null;
	            th2 = null;
	            
	            logFile.writeLog("["+serverLocation+" Server] " +patientID +": "+ resultStr);
	            
	            return resultStr;
	        
	        }catch (InterruptedException e)	{
	            e.printStackTrace();
	            logFile.writeLog("["+serverLocation+" Server] ERROR: listing shcedule failed, Internal error.");
	            return "FAIL";
	        }
		}else{
			logFile.writeLog("getAppointmentSchedule: Invalid inputs");
			return "FAIL";
		}      
	}

	
	
	
	
	
	
	
	
	@Override
	public String cancelAppointment(String patientID, String appointmentID){
		
		if(validateAppointmentID(appointmentID) && validatePatientID(patientID)){
			String result = "";
			UDPclient cancel= null;
			String request = "cncl"+patientID+appointmentID;
			
			if(appointmentID.substring(0,3).equals(serverLocation)){
				result = cancelLocalAppointment(patientID, appointmentID);
				return result;
			}
			if(appointmentID.substring(0,3).equals("MTL")){
				cancel = new UDPclient(serverLocation, "MTL", mtlHost, udpPortMtl, request);
			}
			else if(appointmentID.substring(0,3).equals("QUE")){
				cancel = new UDPclient(serverLocation, "QUE", queHost, udpPortQue, request);
			}
			else if(appointmentID.substring(0,3).equals("SHE")){
				cancel = new UDPclient(serverLocation, "SHE", sheHost, udpPortShe, request);
			}
			
			cancel.start();
			try {
		           cancel.join();
		           logFile.writeLog("["+serverLocation+" Server]: Got the reply of cancelAppointment() request from "+cancel.remoteServer+" server. \""+cancel.replyFromServer+"\"");
		           //create return message
		           result = cancel.replyFromServer;
		           //release thread object
		           cancel = null;
		           return result;
		        }catch (InterruptedException e)	{
		            e.printStackTrace();
		            logFile.writeLog("["+serverLocation+" Server] ERROR: cancelAppointment() failed, Internal error.");
		            return "FAIL";
		        }
		}else {
			logFile.writeLog("cancelAppointment: Inputs invalid");
			return "FAIL";
			
		}
		
	}
	
	
	
	
	@Override
	public synchronized String swapAppointment(String patientID, String oldAppointmentID, String oldAppointmentType,
			String newAppointmentID, String newAppointmentType) {
		
		if(validatePatientID(patientID) && validateAppointmentID(oldAppointmentID) && validateAppointmentID(newAppointmentID) && validateAppointmentType(oldAppointmentType) 
				&& validateAppointmentType(newAppointmentType)){
		
			String resultCancel = "";
			String resultBook = "";
			
			if(oldAppointmentID.substring(0,3).equals(serverLocation)){
				resultCancel = cancelAppointment(patientID, oldAppointmentID);
			}
			else {
				resultCancel = cancelAppointment(patientID, oldAppointmentID);
			}
				
			if(newAppointmentID.substring(0, 3).equals(serverLocation)){
				resultBook = bookAppointment(patientID, newAppointmentID, newAppointmentType);
			}
			else
			{
				resultBook = bookAppointment(patientID, newAppointmentID, newAppointmentType);
			}
			
			logFile.writeLog("Appointment swapped: "+patientID+ ": Old appointment cancelled: "+resultCancel+" New appointment booked:" + resultBook);
			return "SUCCESS";
		}else {
			logFile.writeLog("swapAppointment: Invalid inputs");
			return "FAIL";
		}
	}
	
	
	
	
	

	@Override
	public int loginAdmin(String adminID) {
		int loginIndex;
		if(!adminID.matches("^(MTL|QUE|SHE)A\\d{4}"))
			return (-1);

        try {
        	//authenticate the last 4 characters from the admin ID
            loginIndex=Integer.parseInt(adminID.substring(4));
        }catch  (NumberFormatException e) {
            return (-1);   
        }
        
        synchronized(adminLogin) {
            if (adminLogin[loginIndex]==1)
                return (-2); 
            else {
                adminLogin[loginIndex]=1;   //successful login
            }
        }
        logFile.writeLog("["+serverLocation+" Server]: "+adminID+" login");
        return (0);
    
	}

	@Override
	public int logoutAdmin(String adminID) {
		int loginIndex;
		if(!adminID.matches("^(MTL|QUE|SHE)(A)\\d{4}"))
			return (-1);
        try {
            loginIndex=Integer.parseInt(adminID.substring(4));
        }catch  (NumberFormatException e) {
            return (-1);   // invalid ID
        }
        synchronized(adminLogin) {
            adminLogin[loginIndex]=0;   //successful logout
        }
        logFile.writeLog("["+serverLocation+" Server]: "+adminID+" logout");
        return (0);
	}

	
	
	@Override
	public int loginPatient(String patientID) {
		int loginIndex;
		if(!patientID.matches("^(MTL|QUE|SHE)(P)\\d{4}"))
			return (-1);
     
        try {
        	//authenticate the last 4 characters from the admin ID
            loginIndex=Integer.parseInt(patientID.substring(4));
        }catch  (NumberFormatException e) {
            return (-1);   
        }
        
        synchronized(patientLogin) {
            if (patientLogin[loginIndex]==1)
                return (-2); 
            else {
                patientLogin[loginIndex]=1;   //successful login
            }
        }
        logFile.writeLog("["+serverLocation+" Server]: "+patientID+" login");
        return (0);
	}

	
	
	
	
	@Override
	public int logoutPatient(String patientID) {
		int loginIndex;
		if(!patientID.matches("^(MTL|QUE|SHE)(P)\\d{4}"))
			return (-1);
        try {
            loginIndex=Integer.parseInt(patientID.substring(4));
        }catch  (NumberFormatException e) {
            return (-1);   // invalid ID
        }
        
        synchronized(patientLogin) {
            patientLogin[loginIndex]=0;   //successful logout
        }
        logFile.writeLog("["+serverLocation+" Server]: "+patientID+" logout");
        return (0);
	}

	
	
	public void returnAppointment(String appointmentType, String appointmentID){
		
		if(!appointmentRecords.isEmpty()){
			for(HashMap.Entry<String, HashMap<String, Integer>> entry1 : appointmentRecords.entrySet()){
				for(HashMap.Entry<String,Integer> entry2: entry1.getValue().entrySet()){
					int capacity = entry2.getValue();
					if(entry1.getKey().equals(appointmentType) && entry2.getKey().equals(appointmentID)){
						capacity ++;
						entry1.getValue().replace(appointmentID, capacity);
					}
					else if(entry1.getKey().equals(appointmentType) && !entry1.getValue().containsKey(appointmentID)){
						capacity = 1;
						entry1.getValue().put(appointmentID, capacity);
					}
				}
			}
			
		}else{
		
			HashMap<String, Integer> InnerMap = new HashMap<String, Integer>();
			InnerMap.put(appointmentID, 11);
			appointmentRecords.put(appointmentType, InnerMap);
			}

	}
	
	
	public synchronized String cancelLocalAppointment(String patientID, String appointmentID) {
		if(!patientRecords.isEmpty()){
			for(HashMap.Entry<String, HashMap<String, ArrayList<String>>> entryPatient: patientRecords.entrySet()){
				for(HashMap.Entry<String, ArrayList<String>> entryPatient2: entryPatient.getValue().entrySet()){
					if(entryPatient.getKey().equals(patientID) && entryPatient2.getValue().contains(appointmentID)){
						entryPatient2.getValue().remove(appointmentID);
						if(entryPatient2.getValue().isEmpty()){
							entryPatient.getValue().clear();
						}
						//add appointment back in appointment records
						returnAppointment(entryPatient2.getKey(), appointmentID);
						logFile.writeLog("Appointment cancelled: "+ patientID+":"+appointmentID);
						return "SUCCESS";
					}
				}
			}
			logFile.writeLog("cancelLocalAppointment: "+ appointmentID + " was not found in patient records");
			return "FAIL";
		}else{
			logFile.writeLog("cancelLocalAppointment: records are empty");
			return "FAIL";
		}
	}
	

	public boolean appointmentIsInRecords(String appointmentType, String appointmentID){
		if(!appointmentRecords.isEmpty()){
			for(HashMap.Entry<String, HashMap<String, Integer>> entry1 : appointmentRecords.entrySet()){
				for(HashMap.Entry<String,Integer> entry2: entry1.getValue().entrySet()){
						if(entry1.getKey().equals(appointmentType) && entry2.getKey().equals(appointmentID) && entry2.getValue()>0){
							return true;
						}
				}
			}
		}
		
		return false;
	}
	
	
	
	
	public int getCapacity(String appointmentType, String appointmentID){
		int capacity = 0;
		if(!appointmentRecords.isEmpty()){
			for(HashMap.Entry<String, HashMap<String, Integer>> entry1 : appointmentRecords.entrySet()){
				for(HashMap.Entry<String,Integer> entry2: entry1.getValue().entrySet()){
						//first check if appointment is available
						if(entry1.getKey().equals(appointmentType) && entry2.getKey().equals(appointmentID) && entry2.getValue()>0){
							capacity = entry2.getValue();
						}
				}
			}
		}
		return capacity;
	}
	
	
	
	
	public void updateCapacity(String appointmentID, String appointmentType, int capacity){
		HashMap<String, Integer> newCapacity = new HashMap<String, Integer>();
		newCapacity.put(appointmentID, capacity);
		appointmentRecords.replace(appointmentType,newCapacity);
	}
	
	
	
	
	public void addNewPatientInRecords(String patientID, String appointmentType, String appointmentID){
		HashMap<String, ArrayList<String>> newPatientEntry = new HashMap<String, ArrayList<String>>();
		ArrayList<String> apps = new ArrayList<String>();
		apps.add(appointmentID);
		newPatientEntry.put(appointmentType, apps);
		patientRecords.put(patientID,newPatientEntry);
	}
	
	
	
	
	public synchronized String bookLocalAppointment(String patientID, String appointmentID, String appointmentType) {
		if(validatePatientID(patientID) && validateAppointmentID(appointmentID) && validateAppointmentType(appointmentType)){
			if(appointmentIsInRecords(appointmentType,appointmentID)){
				int capacity = getCapacity(appointmentType,appointmentID);
				if(!patientRecords.isEmpty()){
					for(HashMap.Entry<String, HashMap<String, ArrayList<String>>> patientIDentry: patientRecords.entrySet()){
						if(patientIDentry.getKey().equals(patientID)){
							for(HashMap.Entry<String, ArrayList<String>> patientEntryTypeID: patientIDentry.getValue().entrySet()){
								if(patientEntryTypeID.getKey().equals(appointmentType)){
									if(patientEntryTypeID.getValue().contains(appointmentID)){
										logFile.writeLog("Booking not successfull. This appointment already exists in patient's record"+appointmentID);
										return "FAIL";
									}
									patientEntryTypeID.getValue().add(appointmentID);
									capacity --;
									updateCapacity(appointmentID, appointmentType, capacity);
									logFile.writeLog("Booking  successfull. New Appointment added to existing type:"+appointmentID);
									return "SUCCESS";
								}
							}//end for
							ArrayList<String> apps = new ArrayList<String>();
							apps.add(appointmentID);
							patientIDentry.getValue().put(appointmentType, apps);
							capacity --;
							updateCapacity(appointmentID, appointmentType, capacity);
							logFile.writeLog("Booked on existing patient: "+ patientID+" Type: "+appointmentType+" Appointment: "+appointmentID);
							return "SUCCESS";
						}
					}//end for
					//after all patient record is looped and patient was not found, add the new entry
					addNewPatientInRecords(patientID, appointmentType, appointmentID);
					capacity --;
					updateCapacity(appointmentID, appointmentType, capacity);
					logFile.writeLog("Booked: Patient: "+ patientID+" Type: "+appointmentType+" Appointment: "+appointmentID);
					return "SUCCESS";
				}
				addNewPatientInRecords(patientID, appointmentType, appointmentID);
				capacity --;
				updateCapacity(appointmentID, appointmentType, capacity);
				logFile.writeLog("Booked: Patient: "+ patientID+" Type: "+appointmentType+" Appointment: "+appointmentID);
				return "SUCCESS";
			}else{
				logFile.writeLog("Booking failed, is not in records");
				return "FAIL";
			}
			}else{
				logFile.writeLog("bookAppointment: Invalid inputs");
				return "FAIL";
			}
		
		
	}
	
	
	
	
	public String getLocalSchedule(String patientID){
		
			if(!patientRecords.isEmpty()){
				String result = "";
				for(HashMap.Entry<String, HashMap<String,ArrayList<String>>> entry1 : patientRecords.entrySet()){
					for(HashMap.Entry<String,ArrayList<String>> entry2 : entry1.getValue().entrySet()){
						if(entry1.getKey().equals(patientID)){
							for(int i=0;i<entry2.getValue().size();i++){
								result = result+ entry2.getKey().toString()+";"+entry2.getValue().get(i)+";";
							}
						}
					}
				}
				logFile.writeLog("getLocalSchedule: Patient "+patientID+":"+result);
				return result;
			}else{
			logFile.writeLog("getLocalSchedule: Patient "+patientID+" has no scheduled appointments");
			return "";
			}
	}
	
	
	
	
	public String listLocalAppointments(String appointmentType){
		
		if(!appointmentRecords.isEmpty()){
			String result = "";
			for(HashMap.Entry<String, HashMap<String, Integer>> entry1 : appointmentRecords.entrySet()){
				for(HashMap.Entry<String,Integer> entry2: entry1.getValue().entrySet()){
					if(entry1.getKey().equals(appointmentType) && entry2.getValue()>0){
						
						result = result+entry2.getKey().toString()+" "+entry2.getValue().toString()+";";
					}
				}
			}
			logFile.writeLog("listLocalAppointments: "+ result);
			return result;
		
		}else{
			logFile.writeLog("listLocalAppointments: records are empty");
			return "";
		}
}


	public static String removeLastChar(String str) {
	    return str.substring(0, str.length() - 1);
	}
	
	

	public static String sortListAvailability(String appointmentAvailability){
	
		String result= "";
        String [] availabilityList=appointmentAvailability.split(";");
        Arrays.sort(availabilityList);
        for(int i = 0;i<availabilityList.length; i++){
        	result = result + availabilityList[i]+";";
        }
        return result;
    }
	
	
	
	
	
	public static String sortAppointmentSchedule(String schedule){
		HashMap <String, ArrayList <String> > sch = new HashMap<String,ArrayList <String>>();
		String result="";
		ArrayList <String> dental = new ArrayList<String>();
		ArrayList <String> physician = new ArrayList<String>();
		ArrayList <String> surgeon = new ArrayList<String>();
		
		if(!schedule.equals("")){
			List<String> parts = Arrays.asList(schedule.split(";"));
			sch.put("Dental", dental);
			sch.put("Physician", physician);
			sch.put("Surgeon", surgeon);
		
			if(!sch.isEmpty()){
				for(int i=0; i<parts.size(); i++){
					for(HashMap.Entry<String, ArrayList <String>> entry : sch.entrySet()){
						if(parts.get(i).equals("Dental") && entry.getKey().equals("Dental")){
							dental.add(parts.get(i+1));
							Collections.sort(dental);
							
						}else if(parts.get(i).equals("Physician") && entry.getKey().equals("Physician")){
							physician.add(parts.get(i+1));
							Collections.sort(physician);
							
						}else if(parts.get(i).equals("Surgeon") && entry.getKey().equals("Surgeon")){
							surgeon.add(parts.get(i+1));
							Collections.sort(surgeon);
							
						}
					}
				}
			}
			//sort the keys
			TreeMap<String, ArrayList<String>> sorted = new TreeMap<>(sch);
			
			
			for(HashMap.Entry<String, ArrayList <String>> entry : sorted.entrySet()){
				for(int i=0; i < entry.getValue().size(); i++){
					result = result + entry.getKey()+";"+entry.getValue().get(i)+";";
				}
			}
	
			return result;
		}
		return result;
	}
	
	
	
	public boolean isAllowedToBook(String patientID){
		String schedule = getAppointmentSchedule(patientID);
		//"Dental;MTLE121236;Dental;SHEA111111;Physician;MTLE121236”
		
		if(!schedule.equals("")){
			schedule.replace("Dental;", "");
			schedule.replace("Physician;", "");
			schedule.replace("Surgeon;", "");
			ArrayList <String> mtlAppointments = new ArrayList<String>();
			ArrayList <String> queAppointments = new ArrayList<String>();
			ArrayList <String> sheAppointments = new ArrayList<String>();
			ArrayList <String> otherCitiesAppointments = new ArrayList<String>();
			List<String> appointments = Arrays.asList(schedule.split(";"));
			for (int i=0; i<appointments.size(); i++){
				if(appointments.get(i).substring(0, 3).equals("MTL"))
					mtlAppointments.add(appointments.get(i));
				else if(appointments.get(i).substring(0, 3).equals("QUE"))
					queAppointments.add(appointments.get(i));
				else 
					sheAppointments.add(appointments.get(i));
			}
			
			if(patientID.substring(0,3).equals("MTL") && queAppointments.size()+sheAppointments.size()>=3){
				
				for (int i=0; i<queAppointments.size(); i++){
					otherCitiesAppointments.add(queAppointments.get(i));
				}
				
				for (int i=0; i<sheAppointments.size(); i++){
					otherCitiesAppointments.add(sheAppointments.get(i));
				}	
			}
			
			if(patientID.substring(0,3).equals("QUE") && mtlAppointments.size()+sheAppointments.size()>=3){
				
				for (int i=0; i<mtlAppointments.size(); i++){
					otherCitiesAppointments.add(mtlAppointments.get(i));
				}
				
				for (int i=0; i<sheAppointments.size(); i++){
					otherCitiesAppointments.add(sheAppointments.get(i));
				}
			}
			
			if(patientID.substring(0,3).equals("SHE") && queAppointments.size()+mtlAppointments.size()>=3){
				
				for (int i=0; i<queAppointments.size(); i++){
					otherCitiesAppointments.add(queAppointments.get(i));
				}
				
				for (int i=0; i<mtlAppointments.size(); i++){
					otherCitiesAppointments.add(mtlAppointments.get(i));
				}	
			}
			
			
			//find appointments with same month and year
			
			
			
		}
		return true;
	}
	
	
	public String showAppointmentRecords() {
		if(!appointmentRecords.isEmpty())
			return appointmentRecords.toString();
		else 
			return "Appointment records are empty";
	}
	
	
	public String showPatientRecords() {
		if(!patientRecords.isEmpty())
			return patientRecords.toString();
		else 
			return "Patient records are empty";
	}
	

	public boolean validateAppointmentID(String id){
		if(!id.matches("^(MTL|QUE|SHE)(E|A|M)\\d{6}"))
			return false;
		else
			return true;
	}
	
	
	public boolean validateAppointmentType(String type){
		if(!type.matches("(Surgeon|Dental|Physician)"))
			return false;
		else
			return true;	
	}
	
	
	public boolean validatePatientID(String id){
		if(!id.matches("^(MTL|QUE|SHE)(P)\\d{4}"))
			return false;
		else
			return true;
	}
	
	
	public boolean validateAdminID(String id){
		if(!id.matches("^(MTL|QUE|SHE)(A)\\d{4}"))
			return false;
		else
			return true;
	}

 

	
}

