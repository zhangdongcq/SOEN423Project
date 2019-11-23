package udp;

import hospital.database.Database;
import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType;
import logging.Logger;
import utility.StringConversion;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer implements Runnable {

    private DatagramSocket datagramSocket;
    private byte[] buffer;
    private String hospitalID;

    public UDPServer(String hospitalID)
    {
        try {
            this.hospitalID = hospitalID;
            datagramSocket = new DatagramSocket(StringConversion.getUDPPortNumber(hospitalID));
        } catch (SocketException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                executeServer();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void executeServer() throws IOException
    {
        buffer = new byte[60000];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(packet);

        String data = new String(packet.getData(), 0, packet.getLength());

        String responseString = invokeDatabase(data);
        buffer = responseString.getBytes();

        packet = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
        datagramSocket.send(packet);
    }

    public synchronized String invokeDatabase(String data)
    {
        Database database = Database.getInstance();
        String[] request = data.split(" ");
        String method = request[0];
        String response;
        switch (method)
        {
            case "listAppointment":
                response = database.listAppointment(getAppointmentType(request[1]));
                Logger.saveLog(data, response, hospitalID);
                break;
            case "bookAppointment":
                response = database.bookAppointment(getAppointmentType(request[1]), request[2], request[3]);
                Logger.saveLog(data, response, hospitalID);
                break;
            case "getAppointmentSchedule":
                response =  database.getAppointmentSchedule(request[1]);
                Logger.saveLog(data, response, hospitalID);
                break;
            case "cancelAppointment":
                response = database.cancelAppointment(request[1], request[2]);
                Logger.saveLog(data, response, hospitalID);
                break;
            case"numberOfAppointmentsInWeek":
                response = Long.toString(database.numberOfAppointmentsInWeek(request[1], request[2]));
                Logger.saveLog(data, response, hospitalID);
                break;
            case "cancelAndHold":
                response = database.cancelAndHold(request[1], request[2], getAppointmentType(request[3]));
                Logger.saveLog(data, response, hospitalID);
                 break;
            case "unholdAndRebook" :
                response = database.unholdAndRebook(request[1], request[2], getAppointmentType(request[3]));
                Logger.saveLog(data, response, hospitalID);
                break;
            case "unhold" :
                response = database.unHold(request[1], getAppointmentType(request[2]));
                Logger.saveLog(data, response, hospitalID);
                break;
            default:
            	System.out.println("Method name" + method + " is invalid");
                response = "FAIL";
                Logger.saveLog(data, response, hospitalID);
                break;
        }
        return response;

    }

    private AppointmentType getAppointmentType(String appointmentType)
    {
        if(appointmentType.equals("Surgeon"))
            return AppointmentType.Surgeon;
        if(appointmentType.equals("Physician"))
            return AppointmentType.Physician;
        else {
            return AppointmentType.Dental;
        }
    }

    @Override
    public  void finalize()
    {
        datagramSocket.close();
    }


}
