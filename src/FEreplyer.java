import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FEreplyer {

	public static void replyToFe(String msg, String feIP, int fePort){
		
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(1111);
            byte[] buffer = msg.getBytes();
            InetAddress aHost = InetAddress.getByName(feIP);

            DatagramPacket reply = new DatagramPacket(buffer, buffer.length, aHost, fePort);
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
