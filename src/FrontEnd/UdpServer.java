package FrontEnd;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class UdpServer extends Thread {
    private static final Logger logger = Logger.getLogger("udp_server");
    private int targetUdpPort;
    private String targetAddress;

    public void setMsgToSend(String msgToSend) {
        this.msgToSend = msgToSend;
    }

    private String msgToSend;
    private Map<String, String[]> allRequestRecords;
    private int timeOut;
    private int counter = 1;
    private boolean resend = false;

    UdpServer(int targetUdpPort, String targetAddress, String msgToSend, Map<String, String[]> allRequestRecords, int timeOut) {
        this.targetUdpPort = targetUdpPort;
        this.targetAddress = targetAddress;
        this.msgToSend = msgToSend;
        this.allRequestRecords = allRequestRecords;
        this.timeOut = timeOut;
    }

    @Override
    public void run() {
        while (counter-- > 0) {
            System.out.println("Request message from the client is : " + msgToSend);
            if (resend) msgToSend += ";RESEND";
            DatagramSocket clientSocket = null;
            DatagramPacket requestTarget = null;
            try {
                clientSocket = new DatagramSocket();
                clientSocket.setSoTimeout(timeOut);
                byte[] toSend = msgToSend.getBytes();
                byte[] receivedMsgBuffer = new byte[1024];

                //Send
                InetAddress serverName2 = InetAddress.getByName(targetAddress);
                requestTarget =
                        new DatagramPacket(toSend, toSend.length, serverName2, targetUdpPort);
                clientSocket.send(requestTarget);

                //Receive
                byte[] repliedData = new byte[1024];
                DatagramPacket reply = new DatagramPacket(repliedData, repliedData.length);
                clientSocket.receive(reply);
                String response = new String(receivedMsgBuffer, 0, reply.getLength());
                if (!response.equals("FAILURE_NOTICE_ACK")) {
                    String sequenceId = response.split("|")[1];
                    String msg = response.split("|")[0];
                    String[] eachResponse = new String[5]; // slot 0 : SEQUENCER. slot 1 : RM1. slot 2 : RM2...
                    eachResponse[0] = msg;
                    if (allRequestRecords.get(sequenceId) == null) {
                        allRequestRecords.put(sequenceId, eachResponse);
                    } else {
                        allRequestRecords.get(sequenceId)[0] = msg;
                    }
                }
            } catch (SocketTimeoutException e) {
                counter++;
                resend = true;
            } catch (SocketException e) {
                logger.log(Level.SEVERE, "Socket: " + e.getMessage());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IO: " + e.getMessage());
            } catch (Exception re) {
                logger.log(Level.SEVERE, "Exception in HelloServer.main: " + re);
            } finally {
                if (clientSocket != null) clientSocket.close();
            }
        }
    }
}

