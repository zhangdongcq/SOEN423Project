
import java.net.*; 

import java.io.*;

//UDPlist used to send reuqests to servers to receive list of available appointments from these servers

public class UDPclient extends Thread{
	private int UDPport;
	private String remoteHost;
	private String localServer;
	public String remoteServer;
	public String message;
	public String replyFromServer;
		
	//Construction method
	public UDPclient (String local,String remote,String host,int port,String msg){
		this.localServer = local;
		this.remoteServer = remote;
		this.remoteHost = host;
		this.UDPport = port;
		this.message = msg;
	}	
	
	//Body of thread
	public void run(){ 
		DatagramSocket aSocket = null;  
		try { 
			aSocket = new DatagramSocket();    
			String m = localServer+message; 
			InetAddress aHost = InetAddress.getByName(remoteHost); 
			DatagramPacket request = new DatagramPacket(m.getBytes(),m.length(),aHost,UDPport); 
			aSocket.send(request);
			byte[] buffer = new byte[1000]; 
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length); 
			aSocket.setSoTimeout(10000); //set time out interval for cases
			try {
				aSocket.receive(reply);
				replyFromServer = new String(reply.getData(),0,reply.getLength());
			}catch (SocketTimeoutException e) {
                // timeout exception.
				replyFromServer = "time out";
            }
		} catch (SocketException e){
			System.out.println("Socket list: " + e.getMessage()); 
		} catch (IOException e){
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(aSocket != null) aSocket.close();
		} 
	} 
}
