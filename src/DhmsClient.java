import java.util.Scanner;

public class DhmsClient {
	 public static void main(String args[]){
	 
		
			 Scanner input = new Scanner(System.in);
			 System.out.println("Enter your ID:");
			 String id = input.next();
			 if(id.substring(3,4).equals("A")){
				 AdminClient admin = new AdminClient(id, id.substring(0,3),"localhost", "1050");
				 admin.start();
				 //release thread
				 admin = null; 
			 }
			 else if(id.substring(3,4).equals("P")){
				 PatientClient patient = new PatientClient(id,id.substring(0,3),"localhost", "1050");
				 patient.start();
				 //release thread
				 patient = null;
			 }
	
	 }
}
