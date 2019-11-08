import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;
import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalHelper;
import hospital.RemotelyInvokableHospitalImpl;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import udp.UDPServer;
import utility.IdValidator;

import java.util.Scanner;

public class MainServer {

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter a valid hospital ID: ");
        String hospitalID = scanner.nextLine();
        while (!IdValidator.isValidHospitalID(hospitalID))
        {
            System.out.println();
            System.out.print("Please enter a valid hospital ID: ");
            hospitalID = scanner.nextLine();
        }
        RemotelyInvokableHospitalImpl.setHospitalID(hospitalID);

        try {
            ORB orb = ORB.init(args, null);

            POA rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPoa.the_POAManager().activate();

            RemotelyInvokableHospitalImpl remotelyInvokableHospital = new RemotelyInvokableHospitalImpl();
            //remotelyInvokableHospital.setOrb(orb);

            org.omg.CORBA.Object objectRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objectRef);

            org.omg.CORBA.Object ref = rootPoa.servant_to_reference(remotelyInvokableHospital);
            RemotelyInvokableHospital hospital = RemotelyInvokableHospitalHelper.narrow(ref);

            NameComponent path[] = ncRef.to_name(hospitalID);
            ncRef.rebind(path, hospital);
            new Thread(new UDPServer(hospitalID)).start();
            System.out.println(hospitalID + " server ready...");

            orb.run();

            System.out.println("Enter any key to exit");
            scanner.nextLine();
            System.exit(0);

        } catch (InvalidName | AdapterInactive | ServantNotActive | WrongPolicy
                | org.omg.CosNaming.NamingContextPackage.InvalidName | CannotProceed
                | NotFound e)
        {
            e.printStackTrace();
        }
    }


}

/*
Use to start nameserver:
    tnameserv -ORBInitialPort 900
 */

