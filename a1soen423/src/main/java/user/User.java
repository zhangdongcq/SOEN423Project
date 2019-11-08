package user;

import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;
import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalHelper;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class User {

    private String userID;
    private String userLocation;
    private int userAccessPort;
    private boolean isAdmin;

    private static final int SHE_PORT = 3000;
    private static final int MTL_PORT = 4000;
    private static final int QUE_PORT = 5000;

    public User(String userID)
    {
        this.userID = userID;
        setUserLocation();
        setUserAccessPort();
        setIsAdmin();
    }

    public String getUserID() {
        return userID;
    }

    public int getUserAccessPort()
    {
        return userAccessPort;
    }

    public String getUserLocation()
    {
        return userLocation;
    }

    public RemotelyInvokableHospital getUsersHospital(org.omg.CORBA.Object corbaObject) throws CannotProceed, NotFound, InvalidName
    {

        NamingContextExt ncRef = NamingContextExtHelper.narrow(corbaObject);
        return RemotelyInvokableHospitalHelper.narrow(ncRef.resolve_str(getUserLocation()));
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }

    private void setUserLocation()
    {
        this.userLocation = userID.substring(0,3);
    }

    private void setUserAccessPort()
    {
        switch (userLocation)
        {
            case "MTL":
                this.userAccessPort =  MTL_PORT;
                break;
            case "SHE":
                this.userAccessPort =  SHE_PORT;
                break;
            case "QUE":
                this.userAccessPort =  QUE_PORT;
                break;
        }
    }

    private void setIsAdmin()
    {
        isAdmin = userID.charAt(3) == 'A';
    }



}
