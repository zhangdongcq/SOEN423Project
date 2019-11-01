package FrontEnd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Properties;
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

import FrontEnd.UDPs.UdpFE2Sequencer;
import FrontEnd.UDPs.UdpListenerOnRMs;
import corbasystem.IFrontEndServer;
import corbasystem.IFrontEndServerHelper;

public class FrontEndSever {
   private static final Logger logger = Logger.getLogger("server");
   public static void main(String[] args) {
      try {
         //Get user input and Register frontend name
         InputStreamReader is = new InputStreamReader(System.in);
         BufferedReader br = new BufferedReader(is);
         String frontEndName;

         // Register FrontEnd name
         logger.log(Level.INFO, "Enter a name for this front end server:");
         frontEndName = (br.readLine()).trim().toLowerCase();
         //Get front end ip address
         String ipAddress = InetAddress.getLocalHost().getHostAddress();

         /**
          * Starts Corba Services
          */
         //Generate and initiate the ORB
         Properties props = new Properties();
         //Initiate the port
         props.put("org.omg.CORBA.ORBInitialPort", "1050");
         //Bind the server
         props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
         //Initiate ORB
         ORB orb = ORB.init(args, props);
         //Get rootPOA reference and activate POA Manager
         POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
         poa.the_POAManager().activate();
         //Initiated a ServerImpl instance
         FrontEndServerImpl server = new FrontEndServerImpl(frontEndName);
         org.omg.CORBA.Object ref = poa.servant_to_reference(server);
         IFrontEndServer href = IFrontEndServerHelper.narrow(ref);
         // Get naming context
         org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
         NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
         //Publish the city into naming service
         NameComponent[] nc = ncRef.to_name(frontEndName);
         ncRef.rebind(nc, href);
         System.out.println("Front End server is ready and waiting......");

         /**
          * Start RMs Listener UDP
          */
         UdpListenerOnRMs listenerOnRMs = new UdpListenerOnRMs();
         listenerOnRMs.start();

         logger.log(Level.INFO, "FrontEnd server is ready.");

         //Block until orb closes
         orb.run();
      } catch (ServantNotActive | WrongPolicy | InvalidName | org.omg.CORBA.ORBPackage.InvalidName | CannotProceed | NotFound | AdapterInactive | IOException servantNotActive) {
         servantNotActive.printStackTrace();
      }
   } // end main
}
