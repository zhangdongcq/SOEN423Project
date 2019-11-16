import java.net.*;
import java.io.*;

//UDP server used to listen and receive requests from clients or other servers and send replies to them

public class UDPserver extends Thread{
	private DhmsServant dhms;
	public int UDPport;
	
	//constructor
	public UDPserver (DhmsServant obj, int port){
		this.dhms = obj;
		this.UDPport = port;
	}
	
	public void run(){ 
		DatagramSocket aSocket = null;
		try{    
			//Create socket and buffer
			aSocket = new DatagramSocket(UDPport); 
			byte[] buffer = new byte[1000];
			
			//loop infinitely
			while(true){     
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);     
				aSocket.receive(request); //listen to request
				//analyze request
				DatagramPacket reply;
				String result;
				//aSocket.setSoTimeout(60000);
				
				String arg = (new String(request.getData(),0,request.getLength())).substring(7);
				
				if ((new String(request.getData(),0,request.getLength())).substring(3,7).equals("list")){
					//right request
					result = dhms.listLocalAppointments(arg);
					reply = new DatagramPacket(result.getBytes(),result.length(), request.getAddress(), request.getPort());   
				}
				else if ((new String(request.getData(),0,request.getLength())).substring(3,7).equals("sche")){
					//right request
					result = dhms.getLocalSchedule(arg);
					reply = new DatagramPacket(result.getBytes(),result.length(), request.getAddress(), request.getPort());   
				}
				else if ((new String(request.getData(),0,request.getLength())).substring(3,7).equals("book")){
					//right request
					//arg = MTLP3333MTLA111111Dental
					String patientID= arg.substring(0,8);
					String appointmentID = arg.substring(8,18);
					String appointmentType = arg.substring(18);
					result = dhms.bookLocalAppointment(patientID, appointmentID, appointmentType);
					reply = new DatagramPacket(result.getBytes(),result.length(), request.getAddress(), request.getPort());   
				}
				else if ((new String(request.getData(),0,request.getLength())).substring(3,7).equals("cncl")){
					//right request
					//arg = MTLP3333MTLA111111
					String patientID= arg.substring(0,8);
					String appointmentID = arg.substring(8,18);
					
					result = dhms.cancelLocalAppointment(patientID, appointmentID);
					reply = new DatagramPacket(result.getBytes(),result.length(), request.getAddress(), request.getPort());   
				}
				else{
					//invalid request
					result = "Invalid request";
					reply = new DatagramPacket(result.getBytes(),result.length(), request.getAddress(), request.getPort());   					
				}  
				//send result
				aSocket.send(reply);
				dhms.logFile.writeLog("["+dhms.serverLocation+" Server]: Received app availabilities for: ("+arg+") request from "+(new String(request.getData(),0,request.getLength())).substring(0,3)+" server and replied \""+result+"\"");
			}    
		} catch (SocketException e){
			System.out.println("Socket Server: " + e.getMessage());   
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(aSocket != null) aSocket.close();
		}
	} 

}
