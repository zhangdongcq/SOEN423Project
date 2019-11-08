package udp;

import utility.StringConversion;

import java.io.IOException;
import java.net.*;

public class UDPClient {

    private DatagramSocket datagramSocket;
    private byte[] buffer;

    public UDPClient()
    {
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e)
        {
            e.printStackTrace();
        }
    }

    public synchronized String sendRequest(String requestMessage, String hospitalID)
    {
        String receivedString = "";
        try {
            buffer = requestMessage.getBytes();
            DatagramPacket datagramPacket =
                    new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), StringConversion.getUDPPortNumber(hospitalID));
            datagramSocket.send(datagramPacket);

            buffer = new byte[60000];
            datagramPacket = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(datagramPacket);
            receivedString = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return receivedString;
    }

    @Override
    public void finalize()
    {
        datagramSocket.close();
    }

}



