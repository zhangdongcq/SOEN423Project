package Client;

import corbasystem.IFrontEndServer;
import corbasystem.IFrontEndServerHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import Client.Utils;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

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
        if(Objects.isNull(_remotelyInvokableHospital))
            setupCorba();
    }

    private static void setupCorba()
    {
        try {
            Properties props = new Properties();
            //Generate and initiate the ORB
            props.put("org.omg.CORBA.ORBInitialPort", "1050");
            props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
            ORB orb = ORB.init(new String[1], props);
            // Get Root naming server
            org.omg.CORBA.Object objRef = null;
            objRef = orb.resolve_initial_references("NameService");

            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            String frontEndServerName = Utils.getCity("MTLA1234"); //TODO make sure this is correct for accessing FE server
            // Get object reference through naming server
            IFrontEndServer iFrontEndServer = IFrontEndServerHelper.narrow(ncRef.resolve_str(frontEndServerName + "_fe"));
            ClientTester.setRemoteObject(iFrontEndServer);

        } catch (InvalidName | NotFound | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        deliverArguments(userID, command, parameters);
    }

    public static String deliverArguments(String userID, List<String> arguments)
    {
        if(Objects.isNull(_remotelyInvokableHospital))
            setupCorba();
        return  _remotelyInvokableHospital.requestHandler(userID, arguments.get(0), getParameters(arguments));
    }

    public static String deliverArguments(String userID, String command, String parameters)
    {
        if(Objects.isNull(_remotelyInvokableHospital))
            setupCorba();
        return  _remotelyInvokableHospital.requestHandler(userID, command, parameters);
    }

    private static void setRemoteObject(IFrontEndServer remotelyInvokableHospital)
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



