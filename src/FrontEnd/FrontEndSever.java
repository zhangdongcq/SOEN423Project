package FrontEnd;

import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import corbasystem.IFrontEndServer;
import corbasystem.IFrontEndServerHelper;

public class FrontEndSever {
   private static final Logger logger = Logger.getLogger("server");

   public static void main(String[] args) {
      try {
    	  	Scanner scanner = new Scanner(System.in);
 	  		System.out.print("Enter the number of RMs: ");
 	  		int numberOfRMs = scanner.nextInt();
 	  		for(int i=0; i<numberOfRMs; i++)
 	  		{
 	  			System.out.print("RM" + (i+1) + "'s number: ");
 	  			FrontEndServerImpl.addRM(scanner.nextInt());
 	  			System.out.println();
 	  		}
 	  		FrontEndServerImpl.setNumberOfRMs(numberOfRMs);
 	  		scanner.close();
    	  	startCorbaServices(args);
    	  	
   	  		
   	  System.out.print("System running with " + numberOfRMs + " RMs...");
      } catch (ServantNotActive | WrongPolicy | InvalidName | org.omg.CORBA.ORBPackage.InvalidName | CannotProceed | NotFound | AdapterInactive | IOException servantNotActive) {
         servantNotActive.printStackTrace();
      }
   } // end main

   public static void startCorbaServices(String[] args) throws ServantNotActive, WrongPolicy,  InvalidName,
           org.omg.CORBA.ORBPackage.InvalidName,  CannotProceed,  NotFound,  AdapterInactive,  IOException
   {
      //TODO: Starts Corba Services
      //Generate and initiate the ORB
      Properties props = new Properties();
      //Initiate the port
      props.put("org.omg.CORBA.ORBInitialPort", "1055");
      //Bind the server
      props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
      //Initiate ORB
      ORB orb = ORB.init(args, props);
      //Get rootPOA reference and activate POA Manager
      POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
      poa.the_POAManager().activate();
      //TODO: Initiated a ServerImpl instance
      FrontEndServerImpl server = new FrontEndServerImpl("frontEnd_fe");
      org.omg.CORBA.Object ref = poa.servant_to_reference(server);
      IFrontEndServer href = IFrontEndServerHelper.narrow(ref);
      // TODO: Get naming context
      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
      NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
      //TODO: Publish the frontend with specified name into naming service
      NameComponent[] nc = ncRef.to_name("frontEnd_fe");
      ncRef.rebind(nc, href);
      System.out.println("frontEnd_fe Front End server is ready and waiting......");

      //Block until orb closes
      logger.log(Level.INFO, "FrontEnd server is ready.");
      orb.run();
   }

}
