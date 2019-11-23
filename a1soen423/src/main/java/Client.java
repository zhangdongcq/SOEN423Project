import RM.FrontEndCommunicator;
import RM.MessageExecutor;
import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;
import javafx.util.Pair;
import logging.Logger;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import user.User;
import RM.SequencerCommunicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class Client {

    public static void main(String[] args) {

        try {
            User user;
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialPort", "1080");
            props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
            ORB orb = ORB.init(args, props);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            while (true) {
                String sequencerString = SequencerCommunicator.receiveFromSequencer(MessageExecutor.getCountFail());
                List<String> requestArguments = Arrays.asList(sequencerString.split(";"));
                if(requestArguments.size() == 2)
                	continue;
                for(int i = 0; i< requestArguments.size(); i++)
                	requestArguments.set(i, ((String)requestArguments.get(i).trim()));
                int userIDLocation = 3;
                user = new User(requestArguments.get(userIDLocation));
                RemotelyInvokableHospital remotelyInvokableHospital = getHospital(user, objRef);
                MessageExecutor.executeRequest(requestArguments, remotelyInvokableHospital);
                Pair<String, Integer> ipPort = MessageExecutor.getIpPort();
                String response = MessageExecutor.getResponse();
                System.out.println("Response to FE: " + response);
                if(MessageExecutor.hasResponse())
                	FrontEndCommunicator.sendResponseToFE(response, ipPort);
                logRequestResponse(requestArguments, response, user);
            }

        } catch (IOException | InvalidName | NotFound | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            e.printStackTrace();
        }
    }

    private static void logRequestResponse(List<String> requestArguments, String response, User user) throws IOException
    {
        String requestString = requestArguments.stream().reduce(" ", (item1, item2) -> item1 + " " + item2);
        Logger.saveLog(requestString, response, user.getUserID());
    }

    private static RemotelyInvokableHospital getHospital(User user, org.omg.CORBA.Object corbaObject)
            throws IOException, NotFound, InvalidName, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        RemotelyInvokableHospital remotelyInvokableHospital = user.getUsersHospital(corbaObject);
        System.out.println("Registry Lookup Completed");
        System.out.println("Welcome " + user.getUserID());
        Logger.saveLog("login", "successful login", user.getUserID());
        return remotelyInvokableHospital;
    }

}
