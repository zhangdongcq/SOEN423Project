import DhmsApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.Random;
import java.util.Scanner;


public class AdminClient extends Thread {

	private static String adminID;
	private String serverName;
	private String serverHost;
	private String serverPort;
	
	public AdminClient (String id, String name, String host, String port){
		
		this.adminID = id;
		this.serverHost = host;
		this.serverPort = port;
		this.serverName = name;
	}
	
	public static void showMenu(){
		System.out.println("\nHello Admin "+adminID+"\n");
        System.out.println("Select one of the following options:");
        System.out.println("1. Add an appointment");
        System.out.println("2. Remove an appointment");
        System.out.println("3. Show appointment availabilities");
        System.out.println("4. Book an appointment");
        System.out.println("5. Show appointment schedule for a patient");
        System.out.println("6. Cancel an appointment");
        System.out.println("7. Swap appointments");
        System.out.println("8. Exit");
	}
	

	public void run(){
		Log logFile = new Log("Admin\\"+adminID+".txt");
		Scanner input = new Scanner(System.in);
		
		 try {
	            ORB orb = ORB.init(new String[]{"-ORBInitialPort",serverPort,"-ORBInitialHost",serverHost},null);
	            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
	            NamingContext ncRef = NamingContextHelper.narrow(objRef);

	            NameComponent nc = new NameComponent(serverName,"");
	            NameComponent path[] = {nc};
	            Dhms reference = DhmsHelper.narrow(ncRef.resolve(path));
	            
	            int login = reference.loginAdmin(adminID);
	           
	            if (login==0){
	                logFile.writeLog("Succeed: "+adminID+" login to the "+serverName+" server on "+serverHost);
	                showMenu();
	                int option = 0;
	                while(true){	                	
		                option = input.nextInt();
		                switch(option){
			                case 1: {
			                	System.out.println("Enter Appointment ID:");
			                	String appointmentID = input.next();
			                	System.out.println("Enter Appointment Type:");
			                	String appointmentType = input.next().toUpperCase();
			                	System.out.println(appointmentType);
			                	System.out.println("Enter capacity:");
			                	String capacity = input.next();
			                	int capacityNum = Integer.parseInt(capacity);
			                	System.out.println(logFile.writeLog(reference.addAppointment(appointmentID, appointmentType,capacityNum)));
			                	showMenu();
			                }break;
			                case 2: {
			                	System.out.println("Enter Appointment ID:");
			                	String appointmentID = input.next();
			                	System.out.println("Enter Appointment Type:");
			                	String appointmentType = input.next().toUpperCase();
			                	System.out.println(logFile.writeLog(reference.removeAppointment(appointmentID, appointmentType)));
			                	showMenu();
			                }break;
			                case 3: {
			                	System.out.println("Enter Appointment Type:");
			                	String appointmentType = input.next().toUpperCase();
			                	System.out.println(logFile.writeLog(reference.listAppointmentAvailability(appointmentType)));
			                	showMenu();
			                }break;
			                case 4: {
			                	System.out.println("Enter Patient ID:");
			                	String patientID = input.next();
			                	System.out.println("Enter Appointment Type:");
			                	String appointmentType = input.next().toUpperCase();
			                	System.out.println(reference.listAppointmentAvailability(appointmentType));
			                	System.out.println("Enter Appointment ID:");
			                	String appointmentID = input.next();
			                	System.out.println(logFile.writeLog(reference.bookAppointment(patientID, appointmentID, appointmentType)));
			                	showMenu();	
			                }break;
			                case 5: {
			                	System.out.println("Enter Patient ID:");
			                	String patientID = input.next();
			                	System.out.println(logFile.writeLog(reference.getAppointmentSchedule(patientID)));
			                	showMenu();
			                }break;
			                case 6: {
			                	System.out.println("Enter Patient ID:");
			                	String patientID = input.next();
			                	System.out.println(reference.getAppointmentSchedule(patientID));
			                	System.out.println("Enter Appointment ID:");
			                	String appointmentID = input.next();
			                	System.out.println(logFile.writeLog(reference.cancelAppointment(patientID, appointmentID)));
			                	showMenu();
			                }break;
			                case 7: {
			                	System.out.println("Enter Patient ID:");
			                	String patientID = input.next();
			                	System.out.println(reference.getAppointmentSchedule(patientID));
			                	System.out.println("Enter Appointment Type:");
			                	String appointmentType = input.next().toUpperCase();
			                	System.out.println(reference.listAppointmentAvailability(appointmentType));
			                	System.out.println("Enter old Appointent Type:");
			                	String oldAppointmentType = input.next().toUpperCase();
			                	System.out.println("Enter old Appointent ID:");
			                	String oldAppointmentID = input.next();
			                	System.out.println("Enter new Appointent Type:");
			                	String newAppointmentType = input.next().toUpperCase();
			                	System.out.println("Enter new Appointent ID:");
			                	String newAppointmentID = input.next();
			                	System.out.println(logFile.writeLog(reference.swapAppointment(patientID, oldAppointmentID, oldAppointmentType, newAppointmentID, newAppointmentType)));
			                	showMenu();
			                }break;
			                case 8: {
			                	reference.logoutAdmin(adminID);
	                            logFile.writeLog("Succeed: "+adminID+" logout from the "+serverName+" server on "+serverHost);
	                            input.close();
	                            System.out.println("Admin logged out");
	                            System.exit(0);
	                            
			                }break;
			                default: 
			                	System.out.println("Invalid option number");
			                
		                }//end of switch
	                }//end of while
	            }//end of if
	            else {
	            	if (login==-2){
	                    System.out.println(logFile.writeLog("Login failed, adminID "+adminID+" is already online."));
	                }
	                else System.out.println(logFile.writeLog("Login failed, adminID "+adminID+" is invalid."));
	            }
	            
		 }catch (Exception e){
	            logFile.writeLog("ERROR: "+adminID+" unknown error;");
	            System.err.println("ERROR: "+e);
	            e.printStackTrace(System.out);
		 }
		          
	}//end run()
	
}//end class
