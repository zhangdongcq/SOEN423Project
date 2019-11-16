package Client;

import corbasystem.IFrontEndServer;

import java.util.List;

public class ClientTester extends Thread {


    private String userID;
    private String command;
    private String parameters;
    private static IFrontEndServer _remotelyInvokableHospital;

    public ClientTester(String userID , List<String> arguments)
    {
        this.userID = userID;
        this.command = arguments.get(0);
        this.parameters =getParameters(arguments);
    }

    @Override
    public void run() {
        deliverArguments(userID, command, parameters);
    }

    public static String deliverArguments(String userID, List<String> arguments)
    {
        return  _remotelyInvokableHospital.requestHandler(userID, arguments.get(0), getParameters(arguments));
    }

    public static String deliverArguments(String userID, String command, String parameters)
    {
        return  _remotelyInvokableHospital.requestHandler(userID, command, parameters);
    }

    public static void setRemoteObject(IFrontEndServer remotelyInvokableHospital)
    {
        _remotelyInvokableHospital = remotelyInvokableHospital;
    }

    private static String getParameters(List<String> request)
    {
        switch (request.get(0))
        {
            case "bookAppointment":
                return String.join(",", request.get(1), request.get(2), request.get(3));
            case "cancelAppointment":
                return String.join(",", request.get(1), request.get(2));
            case "swapAppointment":
                return String.join(",", request.get(1), request.get(2), request.get(3), request.get(4), request.get(5));
            case "getAppointmentSchedule":
                return String.join(",", request.get(1));
            case "listAppointmentAvailability":
                return String.join(",", request.get(1));
            case "addAppointment":
                return String.join(",", request.get(1), request.get(2), request.get(3));
            case "removeAppointment":
                return String.join(",", request.get(1), request.get(2));
            default:
                return "ERROR";
        }
    }

}



