import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class SequencerListener {
	
	public static int myUdpPort = 6790;
	public static int seqPort = 6789;
	
	
	public static String getSequencerRequest(){
		MulticastSocket aSocket = null;
		try{    
			//Create socket and buffer
			aSocket = new MulticastSocket(myUdpPort); 
			aSocket.joinGroup(InetAddress.getByName("228.5.6.9"));
			byte[] buffer = new byte[1000];
			DatagramPacket request = new DatagramPacket(buffer, buffer.length);     
			aSocket.receive(request); //listen to request
			String msgFromSeq = (new String(request.getData(),0,request.getLength()));
			
			return msgFromSeq;
			
		}catch (SocketException e){
				System.out.println("Socket Server: " + e.getMessage());   
			} catch (IOException e) {
				System.out.println("IO: " + e.getMessage());
			} finally {
				if(aSocket != null) aSocket.close();
			}
		return "";
			
	}
	
	
	
	public static void sendAck(String ack){
		DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            byte[] buffer = ack.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length, aHost, seqPort);
            aSocket.send(reply);
            
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
	}
	
	
}
