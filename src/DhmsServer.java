import DhmsApp.*;


import DhmsApp.Dhms;
import DhmsApp.DhmsHelper;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;


//the server class
public class DhmsServer {

	 public static void main(String args[]){
	        try {
	        	ORB orb = ORB.init(args, null);
	        	POA rootpoa = (POA)orb.resolve_initial_references("RootPOA");
	            rootpoa.the_POAManager().activate();
	                
	            DhmsServant MTL = new DhmsServant(args[3],args[1],"MTL");
	            DhmsServant QUE = new DhmsServant(args[3],args[1],"QUE");
	            DhmsServant SHE = new DhmsServant(args[3],args[1],"SHE");
	            	
	            //add some initial data in the servers
	            MTL.addAppointment("MTLE232323", "PHYSICIAN", 5);
	            MTL.addAppointment("MTLE343434", "SURGEON", 4);
	            MTL.addAppointment("MTLM232323", "DENTAL", 3);
	            MTL.addAppointment("MTLM121212", "DENTAL", 2);
	            
	            QUE.addAppointment("QUEE232323", "PHYSICIAN", 5);
	            QUE.addAppointment("QUEE343434", "SURGEON", 4);
	            QUE.addAppointment("QUEM232323", "DENTAL", 3);
	            QUE.addAppointment("QUEM121212", "DENTAL", 2);
	            
	            SHE.addAppointment("SHEE232323", "PHYSICIAN", 5);
	            SHE.addAppointment("SHEE343434", "SURGEON", 4);
	            SHE.addAppointment("SHEM232323", "DENTAL", 3);
	            SHE.addAppointment("SHEM121212", "DENTAL", 2);
	            

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
	            
	            System.out.println("Three servers are up and running, registered to name service on "+args[3]+":"+args[1]);
	       
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
	

