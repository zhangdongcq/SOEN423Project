package RM;

import java.io.IOException;
import java.net.*;

public class SequencerCommunicator {


    public static String receiveFromSequencer(int countFail) {

        MulticastSocket aSocket = null;
        try {
            aSocket = new MulticastSocket(6790);
            System.out.println("Replica Manager 1111 Started............");
            aSocket.joinGrougit p(InetAddress.getByName("228.5.6.9"));
            byte[] buffer = new byte[1000];
            while (countFail<3) {
                DatagramPacket requestFromSequencer = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(requestFromSequencer);

                return new String(requestFromSequencer.getData());
            }
            System.err.println("FailCount exceeds 3, replica has failed!");
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }  finally {
            if (aSocket != null)
                aSocket.close();
        }
        return "";
    }

}
