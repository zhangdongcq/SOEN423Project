import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;
import interaction.Menu;
import interaction.CorbaDelivery;
import logging.Logger;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import user.User;
import utility.IdValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {
            User user = getUser(scanner);
            ORB orb = ORB.init(args, null);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            RemotelyInvokableHospital remotelyInvokableHospital = getHospital(user, objRef);

            while (true) {
                String response;
                ArrayList<String> menuArguments = Menu.getRequest(user, scanner);
                String requestString = menuArguments.stream().reduce(" ", (item1, item2) -> item1 + " " + item2);
                response = CorbaDelivery.deliverCorbaRequest(menuArguments, remotelyInvokableHospital);

                Logger.saveLog(requestString, response, user.getUserID());
                System.out.println(response);
                if (menuArguments.get(0).equals("logout")) {
                    user = getUser(scanner);
                    remotelyInvokableHospital = user.getUsersHospital(objRef);
                }
            }

        } catch (IOException | InvalidName | NotFound | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            e.printStackTrace();
        }
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

    private static User getUser(Scanner scanner)
    {
        System.out.println("Please enter your userID");
        String userID = scanner.nextLine();
        while(!IdValidator.isValidUserID(userID))
        {
            System.out.println("Invalid userID, please enter your userID");
            userID = scanner.nextLine();
        }
        return new User(userID);
    }

}
