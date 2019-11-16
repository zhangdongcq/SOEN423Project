import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPserverRM extends Thread{
	
	
		private ReplicaManager rm;
		public int udpPort;
		
		//constructor
		public UDPserverRM (ReplicaManager obj, int port){
			this.rm = obj;
			this.udpPort = port;
		}
		
		public void run(){ 
			DatagramSocket aSocket = null;
			try{    
				//Create socket and buffer
				aSocket = new DatagramSocket(udpPort); 
				byte[] buffer = new byte[1000];
				boolean running = true;
				
				//loop infinitely
				while(running){     
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);     
					aSocket.receive(request); //listen to request
					//analyze request
					DatagramPacket reply;
					String result;
					String msgFromSeq = (new String(request.getData(),0,request.getLength()));
					String [] parts = msgFromSeq.split(";");
					if(parts[0].equals(rm.getRmID())){
						running = false;
						continue;
					}else {
						String feHost = parts[0];
						InetAddress aHost = InetAddress.getByName(feHost);
						int fePort = Integer.valueOf(parts[1]);
						int sequenceNum = Integer.valueOf(parts[2]);
						String userID = parts[3];
						String command = parts[4];
						String arguments = parts[5];
						result = rm.digest(sequenceNum, userID, command, arguments);
						//in case where the received request has a sequenceNum less than the expected, we start listening again
						if(result.equals("dontsend")){
							continue;
						}
						else{
							reply = new DatagramPacket(result.getBytes(),result.length(), aHost, fePort);   					
							aSocket.send(reply);
							rm.logFile.writeLog("RM"+rm.getRmID()+" received "+command+" request from IP: "+parts[0]+" Port: "+parts[1]+" and sent back a reply:"+ result);
							
							//after a reply is sent, currentSequenceNum is increased, so lets check the buffer for potential packets
							result = rm.processBuffer();
								//in case it didnt find a request to be processed
								if(result.equals("dontsend"))
									continue;
								else{
								reply = new DatagramPacket(result.getBytes(),result.length(), aHost, fePort);
								aSocket.send(reply);
								rm.logFile.writeLog("RM"+rm.getRmID()+" processed buffer and sent"+ result);
								}
						}
					}
				
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
