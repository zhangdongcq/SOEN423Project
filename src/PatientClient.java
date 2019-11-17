 import DhmsApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.Random;
import java.util.Scanner;

public class PatientClient extends Thread {
	private static String patientID;
	private String serverName;
	private String serverHost;
	private String serverPort;
	
	public PatientClient (String id, String name,String host, String port){
		this.patientID = id;
		this.serverHost = host;
		this.serverPort = port;
		this.serverName = name;
		
	}
	
	public static void showMenu(){
		System.out.println("\nHello Patient "+patientID+"\n");
        System.out.println("Select one of the following options:");      
        System.out.println("1. Book an appointment");
        System.out.println("2. Show appointment schedule for a patient");
        System.out.println("3. Cancel an appointment");
        System.out.println("4. Swap appointments");
        System.out.println("5. Exit");
	}
	
	

	public void run(){
		
		Log logFileP = new Log("Patient\\"+patientID+".txt");
		Scanner input = new Scanner(System.in);
		 try {
			 	
	            ORB orb = ORB.init(new String[]{"-ORBInitialPort",serverPort,"-ORBInitialHost",serverHost},null);
	            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
	            NamingContext ncRef = NamingContextHelper.narrow(objRef);

	            NameComponent nc = new NameComponent(serverName,"");
	            NameComponent path[] = {nc};
	            Dhms reference = DhmsHelper.narrow(ncRef.resolve(path));
	            
	            int login = reference.loginPatient(patientID);
	            
	            if (login==0){
	                //login succeed
	                logFileP.writeLog("Succeed: "+patientID+" login to the "+serverName+" server on "+serverHost);
	                showMenu();
	                int option = 0;
	                while(true){
	                	option = input.nextInt();
		                switch(option){
			               
			                case 1: {
			                	System.out.println("Enter Appointment Type:");
			                	String appointmentType = input.next();
			                	appointmentType = appointmentType.substring(0,1).toUpperCase() + appointmentType.substring(1).toLowerCase();
			                	System.out.println(reference.listAppointmentAvailability(appointmentType));
			                	System.out.println("Enter Appointment ID:");
			                	String appointmentID = input.next();
			                	System.out.println(logFileP.writeLog(reference.bookAppointment(patientID, appointmentID, appointmentType)));
			                	showMenu();
			                }break;
			                case 2: {
			                	System.out.println(logFileP.writeLog(reference.getAppointmentSchedule(patientID)));
			                	showMenu();
			                }break;
			                case 3: {
			                	System.out.println(reference.getAppointmentSchedule(patientID));
			                	System.out.println("Enter Appointment ID:");
			                	String appointmentID = input.next();
			                	System.out.println(logFileP.writeLog(reference.cancelAppointment(patientID, appointmentID)));
			                	showMenu(); 	
			                }break;
			                case 4:{
			                	System.out.println("Booked appointments: "+ reference.getAppointmentSchedule(patientID));
			                	System.out.println("Enter old Appointent Type:");
			                	String oldAppointmentType = input.next();
			                	oldAppointmentType = oldAppointmentType.substring(0,1).toUpperCase() + oldAppointmentType.substring(1).toLowerCase();
			                	System.out.println("Enter old Appointent ID:");
			                	String oldAppointmentID = input.next();
			                	System.out.println("Enter new Appointent Type:");
			                	String newAppointmentType = input.next();
			                	newAppointmentType = newAppointmentType.substring(0,1).toUpperCase() + newAppointmentType.substring(1).toLowerCase();
			                	System.out.println("Choose appointment: "+ reference.listAppointmentAvailability(newAppointmentType));
			                	System.out.println("Enter new Appointent ID:");
			                	String newAppointmentID = input.next();
			                	System.out.println(logFileP.writeLog(reference.swapAppointment(patientID, oldAppointmentID, oldAppointmentType, newAppointmentID, newAppointmentType)));
			                	showMenu();
			                }break;
			                case 5: {
			                	reference.logoutPatient(patientID);
			                	logFileP.writeLog("Succeed: "+patientID+" logout from the "+serverName+" server on "+serverHost);
			                	input.close();
			                	System.out.println("Patient "+patientID+" logged out");
			                	System.exit(0);
			                }break;
			                default: 
			                	System.out.println("Invalid option number");
			                
		                }//end of switch
	                }//end while
	            }//end of if
	            else {
	            	if (login==-2){
	                    System.out.println(logFileP.writeLog("Login failed, Patient "+patientID+" is already online."));
	                }
	                else System.out.println(logFileP.writeLog("Login failed, Patient "+patientID+" is invalid."));
	            }
	            
		 }catch (Exception e){
	            logFileP.writeLog("ERROR: "+patientID+" unknown error;");
	            System.err.println("ERROR: "+e);
	            e.printStackTrace(System.out);
		 }
	}
}//end of class
