package ReplicaManagers.Dong.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Properties;
import java.util.logging.FileHandler;
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

import ReplicaManagers.Dong.corbasystem_RM.IServer;
import ReplicaManagers.Dong.corbasystem_RM.IServerHelper;
import ReplicaManagers.Dong.utils_server.Utils;

public class Server {
   private static final Logger logger = Logger.getLogger("ReplicaManagers/Dong/server");
   public static void main(String[] args) {
      try {
         //Get user input and Register city
         InputStreamReader is = new InputStreamReader(System.in);
         BufferedReader br = new BufferedReader(is);
         String city;
         // Register Server City
         logger.log(Level.INFO, "Enter the city:");
         city = (br.readLine()).trim().toLowerCase();
         while (!Utils.isValidCityInput(city)) {
            logger.log(Level.WARNING, "Invalid city! Input another one.");
            city = (br.readLine()).trim().toLowerCase();
         }

         // Set Logger FileHandler
         try {
            FileHandler fileHandler = new FileHandler(city + "_server.log");
            fileHandler.setLevel(Level.INFO);
            logger.addHandler(fileHandler);
         } catch (IOException e) {
            logger.log(Level.SEVERE, "File logger is not working.", e);
         }

         //Generate and initiate the ORB
         Properties props = new Properties();
         //Initiate the port
         props.put("org.omg.CORBA.ORBInitialPort", "1050");
         //Bind the ReplicaManagers.Dong.server
         props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
         //Initate ORB
         ORB orb = ORB.init(args, props);
         //Get rootPOA reference and activate POA Manager
         POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
         poa.the_POAManager().activate();
         //Initiated a ServerImpl instance
         ServerImpl server = new ServerImpl();
         server.setServerName(city);
         org.omg.CORBA.Object ref = poa.servant_to_reference(server);
         IServer href = IServerHelper.narrow(ref);
         // Get naming context
         org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
         NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
         //Publish the city into naming service
         String name = city;
         NameComponent[] nc = ncRef.to_name(name);
         ncRef.rebind(nc, href);
         System.out.println(city.toUpperCase() + " ReplicaManagers.Dong.server ready and waiting......");

         /**
          * UDP Server Thread runs first
          */
         UdpServer udpServer = new UdpServer(city, server);
         udpServer.start();
         logger.log(Level.INFO, String.format("UDP ReplicaManagers.Dong.server %s ReplicaManagers.Dong.server is ready.", city));

         //Block until orb closes
         orb.run();
      } catch (ServantNotActive | WrongPolicy | InvalidName | org.omg.CORBA.ORBPackage.InvalidName | CannotProceed | NotFound | AdapterInactive | IOException servantNotActive) {
         servantNotActive.printStackTrace();
      }
   } // end main
}

class UdpServer extends Thread {
   private static final Logger logger = Logger.getLogger("udp_server");
   private DatagramSocket serverSocket;
   private String city;
   private ServerImpl exportedObj;

   UdpServer(String city, ServerImpl exportedObj) {
      this.city = city;
      this.exportedObj = exportedObj;
   }

   @Override
   public void run() {
      while (true) {
         try {
            //Receive Request
            /**
             * case "mtl": return 5678;
             * case "que": return 5677;
             * case "she": return 5676;
             */
            int localUdpPort = Utils.getUdpPort(city);
            serverSocket = new DatagramSocket(localUdpPort);
            byte[] buffer = new byte[1024];

            DatagramPacket requestPackage = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(requestPackage);
            String requestString = new String(buffer, 0, requestPackage.getLength());
            String[] decodedRequest = requestString.split("\\.");//decodedRequest[0] = command, decodedRequest[0] = param

            //Send Reply
            InetAddress clientAddress = requestPackage.getAddress();
            int clientPort = requestPackage.getPort();
            byte[] replyData;
            DatagramPacket reply;
            if (decodedRequest[0].equalsIgnoreCase(Utils.LIST_APP_AVAILABILITY)) {
               String localAppAvailable = exportedObj.getLocalAppointmentAvailability(decodedRequest[1]);
               replyData = localAppAvailable.getBytes();
            } else if (decodedRequest[0].equalsIgnoreCase(Utils.GET_APP_SCHE)) {
               String getLocalAppSchedule = exportedObj.getLocalAppSchedule(decodedRequest[1]);
               replyData = getLocalAppSchedule.getBytes();
            } else {
               String bookAppointmentFromRemoteServer = exportedObj.bookAppointment(decodedRequest[1], decodedRequest[2], decodedRequest[3]);
               replyData = bookAppointmentFromRemoteServer.getBytes();
            }

            reply = new DatagramPacket(replyData, replyData.length, clientAddress, clientPort);

            serverSocket.send(reply);
            serverSocket.close();

         } catch (SocketException e) {
            logger.log(Level.SEVERE, "Socket: " + e.getMessage());
         } catch (IOException e) {
            logger.log(Level.SEVERE, "IO: " + e.getMessage());
         } catch (Exception re) {
            logger.log(Level.SEVERE, "Exception in HelloServer.main: " + re);
         } finally {
            if (serverSocket != null) serverSocket.close();
         }
      }
   }
}
