package FrontEnd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import corbasystem.IFrontEndServer;
import corbasystem.IFrontEndServerHelper;

public class FrontEndSever {
   private static final Logger logger = Logger.getLogger("server");

   public static void main(String[] args) {
      try {
         String frontEndName = getAndSetFrontEndName();
         startCorbaServices(args, frontEndName);
      } catch (ServantNotActive | WrongPolicy | InvalidName | org.omg.CORBA.ORBPackage.InvalidName | CannotProceed | NotFound | AdapterInactive | IOException servantNotActive) {
         servantNotActive.printStackTrace();
      }
   } // end main

   public static void startCorbaServices(String[] args, String frontEndName) throws ServantNotActive, WrongPolicy,  InvalidName,
           org.omg.CORBA.ORBPackage.InvalidName,  CannotProceed,  NotFound,  AdapterInactive,  IOException
   {
      //TODO: Starts Corba Services
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
      //TODO: Initiated a ServerImpl instance
      FrontEndServerImpl server = new FrontEndServerImpl(frontEndName);
      org.omg.CORBA.Object ref = poa.servant_to_reference(server);
      IFrontEndServer href = IFrontEndServerHelper.narrow(ref);
      // TODO: Get naming context
      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
      NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
      //TODO: Publish the frontend with specified name into naming service
      NameComponent[] nc = ncRef.to_name(frontEndName+"_fe");
      ncRef.rebind(nc, href);
      System.out.println(frontEndName + "_fe Front End server is ready and waiting......");

      //Block until orb closes
      logger.log(Level.INFO, "FrontEnd server is ready.");
      orb.run();
   }

   public static String getAndSetFrontEndName() throws IOException {
      //Get user input and Register frontend name
      InputStreamReader is = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(is);
      String frontEndName;
      logger.log(Level.INFO, "Enter the city:");
      frontEndName = (br.readLine()).trim().toLowerCase();
      while (!Utils.isValidCityInput(frontEndName)) {
         logger.log(Level.WARNING, "Invalid city! mtl, que, she are options. Input another one.");
         frontEndName = (br.readLine()).trim().toLowerCase();
      }
      return frontEndName;
   }
}
