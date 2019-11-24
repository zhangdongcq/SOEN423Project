import DhmsApp.*;


import DhmsApp.Dhms;
import DhmsApp.DhmsHelper;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;


//the server class
public class DhmsServer {

	 public static void main(String args[]){
	        try {
	        	
	        	Properties props = new Properties();
	            //Generate and initiate the ORB
	            props.put("org.omg.CORBA.ORBInitialPort", "1055");
	            props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
	            ORB orb = ORB.init(args, props);
	        	
	        	POA rootpoa = (POA)orb.resolve_initial_references("RootPOA");
	            rootpoa.the_POAManager().activate();
	                
	            DhmsServant MTL = new DhmsServant("localhost","1055","MTL");
	            DhmsServant QUE = new DhmsServant("localhost","1055","QUE");
	            DhmsServant SHE = new DhmsServant("localhost","1055","SHE");
	            	

	            org.omg.CORBA.Object refMTL = rootpoa.servant_to_reference(MTL);
	            Dhms hrefMTL = DhmsHelper.narrow(refMTL);
	                
	            org.omg.CORBA.Object refQUE = rootpoa.servant_to_reference(QUE);
	            Dhms hrefQUE = DhmsHelper.narrow(refQUE);
	                
	            org.omg.CORBA.Object refSHE = rootpoa.servant_to_reference(SHE);
	            Dhms hrefSHE = DhmsHelper.narrow(refSHE);
	                
	            NameComponent nc = new NameComponent("MTL","");
	            NameComponent nc2 = new NameComponent("QUE","");
	            NameComponent nc3 = new NameComponent("SHE","");
	            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
	            NamingContext ncRef = NamingContextHelper.narrow(objRef);

	            NameComponent path[] = {nc};
	            ncRef.rebind(path, hrefMTL);
    
	            path = new NameComponent[] {nc2};
	            ncRef.rebind(path, hrefQUE);

	            path = new NameComponent[] {nc3};
	            ncRef.rebind(path, hrefSHE);
	            
	            System.out.println("Three servers are up and running, registered to name service on localhost:1055");
	       
	            java.lang.Object sync = new java.lang.Object();
	            synchronized(sync) {
	                sync.wait();
	            }
	             
	        }catch (Exception e){
	            System.err.println("ERROR:"+e);
	            e.printStackTrace(System.out);
	        }
	    }
	}
	

