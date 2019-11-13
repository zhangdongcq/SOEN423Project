import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;
import interaction.CorbaTester;
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
import static org.junit.Assert.assertTrue;

public class Assignment1Tests {


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
    public void givenPatientBooks3AppointmentsInAWeekInOtherHospitals_callingSwapAppointment_returnsFail()
    {
        setUser("SHEA1111");
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA121236","Dental");
        List<String> removeAppointment2 = Arrays.asList("removeAppointment", "SHEA121236","Physician");
        List<String> removeAppointment3 = Arrays.asList("removeAppointment", "SHEA121236","Surgeon");
        List<String> removeAppointment4 = Arrays.asList("removeAppointment", "QUEA101236","Dental");
        List<String> removeAppointment5 = Arrays.asList("removeAppointment", "QUEA111119" ,"Dental");

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment2, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment3, remotelyInvokableHospital);
        setUser("QUEA1111");
        CorbaTester.deliverArguments(removeAppointment4, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment5, remotelyInvokableHospital);
        setUser("SHEA1111");

        List<String> addAppointment = Arrays.asList("addAppointment", "SHEA121236","Dental","1");
        List<String> addAppointment2 = Arrays.asList("addAppointment", "SHEA121236","Physician","1");
        List<String> addAppointment3 = Arrays.asList("addAppointment", "SHEA121236","Surgeon","1");
        List<String> addAppointment4 = Arrays.asList("addAppointment", "QUEA101236","Dental","1");
        List<String> addAppointment5 = Arrays.asList("addAppointment", "QUEA111119","Dental","1");

        CorbaTester.deliverArguments(addAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment2, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment3, remotelyInvokableHospital);
        setUser("QUEA1111");
        CorbaTester.deliverArguments(addAppointment4, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment5, remotelyInvokableHospital);
        setUser("SHEA1111");

        List<String> bookAppointment1 = Arrays.asList("bookAppointment","MTLP1222", "SHEA121236","Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "MTLP1222","SHEA121236","Physician");
        List<String> bookAppointment3 = Arrays.asList("bookAppointment","MTLP1222", "SHEA121236","Surgeon");
        List<String> bookAppointment4 = Arrays.asList("bookAppointment","MTLP1222", "QUEA111119","Dental");
        List<String> swapAppointment = Arrays.asList("swapAppointment","MTLP1222", "QUEA111119","Dental", "QUEA101236","Dental");

        String result = CorbaTester.deliverArguments(bookAppointment1, remotelyInvokableHospital);
        assertEquals("SUCCESS", result);
        result = CorbaTester.deliverArguments(bookAppointment2, remotelyInvokableHospital);
        assertEquals("SUCCESS", result);
        result = CorbaTester.deliverArguments(bookAppointment3, remotelyInvokableHospital);
        assertEquals("SUCCESS", result);
        result = CorbaTester.deliverArguments(bookAppointment4, remotelyInvokableHospital);
        assertEquals("SUCCESS", result);
        result = CorbaTester.deliverArguments(swapAppointment, remotelyInvokableHospital);
        assertEquals("FAIL", result);

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment2, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment3, remotelyInvokableHospital);
        setUser("QUEA1111");
        CorbaTester.deliverArguments(removeAppointment4, remotelyInvokableHospital);

    }


    @Test
    public void givenAppointmentFull_callingBookAppointment_returnsFailure()
    {
        setUser("SHEA1111");
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA111136","Dental");
        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        List<String> addAppointment = Arrays.asList("addAppointment", "SHEA111136","Dental","1");
        List<String> bookAppointment1 = Arrays.asList("bookAppointment", "MTLP1532", "SHEA111136", "Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "SHEP1532", "SHEA111136", "Dental");


        CorbaTester.deliverArguments(addAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(bookAppointment1, remotelyInvokableHospital);
        String result = CorbaTester.deliverArguments(bookAppointment2, remotelyInvokableHospital);
        assertEquals("FAIL", result);
        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
    }

    @Test
    public void givenPatientBooksAnAppointment_bookingSameTypeDay_returnsFail()
    {
        setUser("MTLA1111");
        List<String> removeAppointment = Arrays.asList("removeAppointment", "MTLE121236","Dental");
        List<String> removeAppointment2 = Arrays.asList("removeAppointment", "SHEA121236","Dental");
        List<String> removeAppointment3 = Arrays.asList("removeAppointment", "MTLE121236","Dental");
        List<String> removeAppointment4 = Arrays.asList("removeAppointment", "MTLE121236","Surgeon");

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment3, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment4, remotelyInvokableHospital);
        setUser("SHEA1111");
        CorbaTester.deliverArguments(removeAppointment2, remotelyInvokableHospital);

        List<String> addAppointment = Arrays.asList("addAppointment", "MTLE121236","Dental","1");
        List<String> addAppointment2 = Arrays.asList("addAppointment", "SHEA121236","Dental","1");
        List<String> addAppointment3 = Arrays.asList("addAppointment", "MTLE121236","Dental","1");
        List<String> addAppointment4 = Arrays.asList("addAppointment", "MTLE121236","Surgeon","1");

        List<String> bookAppointment1 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "MTLP1234", "SHEA121236", "Dental");
        List<String> bookAppointment3 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Dental");
        List<String> bookAppointment4 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Surgeon");


        CorbaTester.deliverArguments(addAppointment2, remotelyInvokableHospital);
        setUser("MTLA1111");
        CorbaTester.deliverArguments(addAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment3, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment4, remotelyInvokableHospital);


        assertEquals("SUCCESS", CorbaTester.deliverArguments(bookAppointment1, remotelyInvokableHospital));
        assertEquals("FAIL",
                CorbaTester.deliverArguments(bookAppointment2, remotelyInvokableHospital));
        assertEquals("FAIL",
                CorbaTester.deliverArguments(bookAppointment3, remotelyInvokableHospital));
        assertEquals("SUCCESS",
                CorbaTester.deliverArguments(bookAppointment4, remotelyInvokableHospital));

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment3, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment4, remotelyInvokableHospital);
        setUser("SHEA1111");
        CorbaTester.deliverArguments(removeAppointment2, remotelyInvokableHospital);
    }

    @Test
    public void givenAppointmentsBookedInDifferentCategoriesandPlaces_callingGetAppointmentSchedule_returnsThem()
    {
        setUser("MTLA1111");
        List<String> removeAppointment = Arrays.asList("removeAppointment", "MTLE121236","Dental");
        List<String> removeAppointment2 = Arrays.asList("removeAppointment", "SHEA121237","Dental");
        List<String> removeAppointment3 = Arrays.asList("removeAppointment", "MTLE121236","Physician");
        List<String> removeAppointment4 = Arrays.asList("removeAppointment", "MTLE121236","Surgeon");

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment3, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment4, remotelyInvokableHospital);
        setUser("SHEA1111");
        CorbaTester.deliverArguments(removeAppointment2, remotelyInvokableHospital);

        List<String> addAppointment = Arrays.asList("addAppointment", "MTLE121236","Dental","1");
        List<String> addAppointment2 = Arrays.asList("addAppointment", "SHEA121237","Dental","1");
        List<String> addAppointment3 = Arrays.asList("addAppointment", "MTLE121236","Physician","1");
        List<String> addAppointment4 = Arrays.asList("addAppointment", "MTLE121236","Surgeon","1");

        CorbaTester.deliverArguments(addAppointment2, remotelyInvokableHospital);
        setUser("MTLA1111");
        CorbaTester.deliverArguments(addAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment3, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment4, remotelyInvokableHospital);

        List<String> bookAppointment1 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "MTLP1234", "SHEA121237", "Dental");
        List<String> bookAppointment3 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Physician");
        List<String> bookAppointment4 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Surgeon");

        CorbaTester.deliverArguments(bookAppointment1, remotelyInvokableHospital);
        CorbaTester.deliverArguments(bookAppointment2, remotelyInvokableHospital);
        CorbaTester.deliverArguments(bookAppointment3, remotelyInvokableHospital);
        CorbaTester.deliverArguments(bookAppointment4, remotelyInvokableHospital);

        List<String> listSchedule = Arrays.asList("getAppointmentSchedule", "MTLP1234");
        String result = CorbaTester.deliverArguments(listSchedule, remotelyInvokableHospital);
        assertEquals(";Dental;MTLE121236;Dental;SHEA121237;Physician;MTLE121236;Surgeon;MTLE121236", result);

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment3, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment4, remotelyInvokableHospital);
        setUser("SHEA1111");
        CorbaTester.deliverArguments(removeAppointment2, remotelyInvokableHospital);
    }

    @Test
    public void givenAdminOfAnotherServer_callingAddRemoveAppointmentReturnsFailure()
    {
        setUser("SHEA1234");
        String result;
        List<String> addAppointment = Arrays.asList("addAppointment","MTLE121236","Dental","1");
        result = CorbaTester.deliverArguments(addAppointment, remotelyInvokableHospital);
        assertEquals("FAIL", result);
        List<String> removeAppointment = Arrays.asList("removeAppointment", "MTLE121236","Dental");
        result = CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        assertEquals("FAIL",result );
    }


    @Test
    public void givenPatientBooks3AppointmentsInAWeekInOtherHospitals_callingBookOnAForth_returnsFail()
    {
        setUser("SHEA1111");
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA121236","Dental");
        List<String> removeAppointment2 = Arrays.asList("removeAppointment", "SHEA121236","Physician");
        List<String> removeAppointment3 = Arrays.asList("removeAppointment", "SHEA121236","Surgeon");
        List<String> removeAppointment4 = Arrays.asList("removeAppointment", "QUEA101236","Dental");

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment2, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment3, remotelyInvokableHospital);
        setUser("QUEA1111");
        CorbaTester.deliverArguments(removeAppointment4, remotelyInvokableHospital);
        setUser("SHEA1111");

        List<String> addAppointment = Arrays.asList("addAppointment", "SHEA121236","Dental","1");
        List<String> addAppointment2 = Arrays.asList("addAppointment", "SHEA121236","Physician","1");
        List<String> addAppointment3 = Arrays.asList("addAppointment", "SHEA121236","Surgeon","1");
        List<String> addAppointment4 = Arrays.asList("addAppointment", "QUEA101236","Dental","1");

        CorbaTester.deliverArguments(addAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment2, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment3, remotelyInvokableHospital);
        setUser("QUEA1111");
        CorbaTester.deliverArguments(addAppointment4, remotelyInvokableHospital);
        setUser("SHEA1111");

        List<String> bookAppointment1 = Arrays.asList("bookAppointment","MTLP1222", "SHEA121236","Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "MTLP1222","SHEA121236","Physician");
        List<String> bookAppointment3 = Arrays.asList("bookAppointment","MTLP1222", "SHEA121236","Surgeon");
        List<String> bookAppointment4 = Arrays.asList("bookAppointment","MTLP1222", "QUEA101236","Dental");

        String result = CorbaTester.deliverArguments(bookAppointment1, remotelyInvokableHospital);
        assertEquals("SUCCESS", result);
        result = CorbaTester.deliverArguments(bookAppointment2, remotelyInvokableHospital);
        assertEquals("SUCCESS", result);
        result = CorbaTester.deliverArguments(bookAppointment3, remotelyInvokableHospital);
        assertEquals("SUCCESS", result);
        result = CorbaTester.deliverArguments(bookAppointment4, remotelyInvokableHospital);
        assertEquals("FAIL", result);

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment2, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment3, remotelyInvokableHospital);
        setUser("QUEA1111");
        CorbaTester.deliverArguments(removeAppointment4, remotelyInvokableHospital);
    }

    @Test
    public void givenPatientsHaveBookedAndFutureAppointmentsExist_callingRemoveAppointmentMovesThemToTheNewAppointment()
    {
        setUser("SHEA1111");
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA101236","Dental");
        List<String> removeAppointment2 = Arrays.asList("removeAppointment", "SHEA121237","Dental");

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment2, remotelyInvokableHospital);

        List<String> addAppointment = Arrays.asList("addAppointment", "SHEA101236","Dental","2");
        List<String> addAppointment2 = Arrays.asList("addAppointment", "SHEA121237","Dental","2");

        CorbaTester.deliverArguments(addAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(addAppointment2, remotelyInvokableHospital);

        List<String> bookAppointment1 = Arrays.asList("bookAppointment","MTLP1233", "SHEA101236","Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "MTLP1234","SHEA101236","Dental");
        CorbaTester.deliverArguments(bookAppointment1, remotelyInvokableHospital);
        CorbaTester.deliverArguments(bookAppointment2, remotelyInvokableHospital);

        List<String> showAppointmentSchedule = Arrays.asList("getAppointmentSchedule", "MTLP1233");
        List<String> showAppointmentSchedule2 = Arrays.asList("getAppointmentSchedule", "MTLP1234");

        String result = CorbaTester.deliverArguments(showAppointmentSchedule, remotelyInvokableHospital);
        assertEquals(";Dental;SHEA101236", result);
        result = CorbaTester.deliverArguments(showAppointmentSchedule2, remotelyInvokableHospital);
        assertEquals(";Dental;SHEA101236", result);

        result = CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        assertEquals("SUCCESS", result);

        result = CorbaTester.deliverArguments(showAppointmentSchedule, remotelyInvokableHospital);
        assertEquals(";Dental;SHEA121237", result);

        result = CorbaTester.deliverArguments(showAppointmentSchedule2, remotelyInvokableHospital);
        assertEquals(";Dental;SHEA121237", result);

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(removeAppointment2, remotelyInvokableHospital);
    }

    @Test
    public void givenPatientBooks_callingListAppointmentAvailability_returnsAReducedValue()
    {
        setUser("SHEA1111");
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA101236","Dental");

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);

        List<String> addAppointment = Arrays.asList("addAppointment", "SHEA101236","Dental","2");
        CorbaTester.deliverArguments(addAppointment, remotelyInvokableHospital);

        List<String> listAppointmentAvailability = Arrays.asList("listAppointmentAvailability","Dental");
        String result = CorbaTester.deliverArguments(listAppointmentAvailability, remotelyInvokableHospital);

        assertTrue(result.contains("SHEA101236 2"));
        List<String> bookAppointment1 = Arrays.asList("bookAppointment","MTLP1233", "SHEA101236","Dental");
        CorbaTester.deliverArguments(bookAppointment1, remotelyInvokableHospital);
        result = CorbaTester.deliverArguments(listAppointmentAvailability, remotelyInvokableHospital);
        assertTrue(result.contains("SHEA101236 1"));

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
    }


}
