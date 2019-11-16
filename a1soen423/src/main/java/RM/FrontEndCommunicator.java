package RM;

import javafx.util.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FrontEndCommunicator {

    public static void sendResponseToFE(String message, Pair<String, Integer> ipPort) {
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            byte[] messageByte = message.getBytes();
            InetAddress aHost = InetAddress.getByName(ipPort.getKey());

            DatagramPacket request = new DatagramPacket(messageByte, messageByte.length, aHost, ipPort.getValue());
            aSocket.send(request);
            System.out.println(message);
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
