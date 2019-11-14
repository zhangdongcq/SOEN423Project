import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;
import interaction.CorbaTester;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import user.User;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Demo2Tests {

    public RemotelyInvokableHospital remotelyInvokableHospital;
    private org.omg.CORBA.Object objRef;

    @Before
    public void setup()
    {
        String[] args = null;
        String userId = "MTLA1212";
        ORB orb = ORB.init(args, null);
        try {
            objRef = orb.resolve_initial_references("NameService");
            setUser(userId);
        } catch (InvalidName e)
        {
            e.printStackTrace();
        }
        setUser("QUEA1234");
        List<String> addAppointment1 = Arrays.asList("addAppointment", "QUEE100619","Physician", "2");
        List<String> addAppointment2 = Arrays.asList("addAppointment", "QUEE110619","Dental", "1");
        CorbaTester.deliverArguments(addAppointment1, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment2, remotelyInvokableHospital);
        setUser("MTLA1234");
        List<String> addAppointment3 = Arrays.asList("addAppointment", "MTLA120619","Physician", "2");
        List<String> addAppointment4 = Arrays.asList("addAppointment", "MTLA140619","Surgeon", "1");
        CorbaTester.deliverArguments(addAppointment3, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment4, remotelyInvokableHospital);
        setUser("SHEA1234");
        List<String> addAppointment5 = Arrays.asList("addAppointment", "SHEA130619","Physician", "1");
        List<String> addAppointment6 = Arrays.asList("addAppointment", "SHEA250619","Dental", "1");
        CorbaTester.deliverArguments(addAppointment5, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment6, remotelyInvokableHospital);
    }

    private void setUser(String userID)
    {
        try {
            User user = new User(userID);
            remotelyInvokableHospital = user.getUsersHospital(objRef);
        }
        catch ( NotFound | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName e)
        {
            e.printStackTrace();
        }

    }

    @Test
    public void givenAppointmentsAreAdded_callingListAppointmentAvailablility_returnsThoseAppointments()
    {
        setUser("SHEA1234");
        List<String> listAppointmentDental = Arrays.asList("listAppointmentAvailability", "Dental");
        List<String>listAppointmentSurgeon = Arrays.asList("listAppointmentAvailability", "Surgeon");
        List<String>listAppointmentPhysician = Arrays.asList("listAppointmentAvailability", "Physician");
        assertEquals(";QUEE110619 1;SHEA250619 1", CorbaTester.deliverArguments(listAppointmentDental, remotelyInvokableHospital));
        assertEquals(";MTLA120619 2;QUEE100619 2;SHEA130619 1", CorbaTester.deliverArguments(listAppointmentPhysician, remotelyInvokableHospital));
        assertEquals(";MTLA140619 1", CorbaTester.deliverArguments(listAppointmentSurgeon, remotelyInvokableHospital));
    }




    //AFTER CLASS

    @Test
    public void removeAllAppointments()
    {
        setUser("QUEA1234");
        List<String> removeAppointment1 = Arrays.asList("removeAppointment", "QUEE100619","Physician", "2");
        List<String> removeAppointment2 = Arrays.asList("removeAppointment", "QUEE110619","Dental", "1");
        CorbaTester.deliverArguments(removeAppointment1, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment2, remotelyInvokableHospital);
        setUser("MTLA1234");
        List<String> removeAppointment3 = Arrays.asList("removeAppointment", "MTLA120619","Physician", "2");
        List<String> removeAppointment4 = Arrays.asList("removeAppointment", "MTLA140619","Surgeon", "1");
        CorbaTester.deliverArguments(removeAppointment3, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment4, remotelyInvokableHospital);
        setUser("SHEA1234");
        List<String> removeAppointment5 = Arrays.asList("removeAppointment", "SHEA130619","Physician", "1");
        List<String> removeAppointment6 = Arrays.asList("removeAppointment", "SHEA250619","Dental", "1");
        CorbaTester.deliverArguments(removeAppointment5, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment6, remotelyInvokableHospital);
    }

}
