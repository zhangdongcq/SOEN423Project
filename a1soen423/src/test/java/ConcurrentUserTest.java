import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;
import interaction.CorbaTester;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import user.User;

import java.util.Arrays;
import java.util.List;

public class ConcurrentUserTest {

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
    public void givenOldAptDoesNotExist_callingSwapAppointment_doesNotBookNewApt(){
        setUser("SHEA1111");
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA111115", "Dental");
        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        List<String> newAppointment = Arrays.asList("addAppointment", "SHEA111115","Dental","1");
        List<String> swapAppointment = Arrays.asList("swapAppointment", "MTLP1532","QUEA111115", "Dental", "SHEA111115", "Surgeon");
        List<String> getAppointmentSchedule = Arrays.asList("getAppointmentSchedule", "MTLP1532");

        CorbaTester.deliverArguments(newAppointment, remotelyInvokableHospital);

        assertEquals("FAIL", CorbaTester.deliverArguments(swapAppointment, remotelyInvokableHospital));
        assertEquals("", CorbaTester.deliverArguments(getAppointmentSchedule, remotelyInvokableHospital));

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
    }

    @Test
    public void givenNewAptIsNotAvailable_callingSwapAppointment_doesNotCancelOldAptOrBookNewOne()
    {
        setUser("SHEA1111");
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA111115", "Dental");
        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
        List<String> addOldAppointment = Arrays.asList("addAppointment", "SHEA111115","Dental","1");
        List<String> bookOldAppointment = Arrays.asList("bookAppointment", "MTLP1532", "SHEA111115", "Dental");
        List<String> swapAppointment = Arrays.asList("swapAppointment", "MTLP1532", "SHEA111115", "Surgeon" ,"QUEA111115", "Dental");
        List<String> getAppointmentSchedule = Arrays.asList("getAppointmentSchedule", "MTLP1532");

        CorbaTester.deliverArguments(addOldAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(bookOldAppointment, remotelyInvokableHospital);

        assertEquals("FAIL",CorbaTester.deliverArguments(swapAppointment, remotelyInvokableHospital));

        assertEquals(";Dental;SHEA111115", CorbaTester.deliverArguments(getAppointmentSchedule, remotelyInvokableHospital));

        CorbaTester.deliverArguments(removeAppointment, remotelyInvokableHospital);
    }

    @Test
    public void givenNewAptExistsAndOldAptExists_callingSwapAppointment_swapsTheAppointments()
    {
        List<String> removeOldAppointment = Arrays.asList("removeAppointment", "SHEA111115", "Dental");
        List<String> removeNewAppointment = Arrays.asList("removeAppointment", "QUEA111115", "Surgeon");

        setUser("QUEA1111");
        CorbaTester.deliverArguments(removeNewAppointment, remotelyInvokableHospital);
        setUser("SHEA1111");
        CorbaTester.deliverArguments(removeOldAppointment, remotelyInvokableHospital);

        List<String> addOldAppointment = Arrays.asList("addAppointment", "SHEA111115","Dental","1");
        List<String> addNewAppointment = Arrays.asList("addAppointment", "QUEA111115","Surgeon","1");
        List<String> bookOldAppointment = Arrays.asList("bookAppointment", "MTLP1532", "SHEA111115", "Dental");
        List<String> swapAppointment = Arrays.asList("swapAppointment", "MTLP1532", "SHEA111115", "Dental" ,"QUEA111115", "Surgeon");
        List<String> getAppointmentSchedule = Arrays.asList("getAppointmentSchedule", "MTLP1532");

        setUser("SHEA1111");
        CorbaTester.deliverArguments(addOldAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(bookOldAppointment, remotelyInvokableHospital);
        setUser("QUEA1111");
        CorbaTester.deliverArguments(addNewAppointment, remotelyInvokableHospital);

        assertEquals("SUCCESS",CorbaTester.deliverArguments(swapAppointment, remotelyInvokableHospital));

        assertEquals(";Surgeon;QUEA111115", CorbaTester.deliverArguments(getAppointmentSchedule, remotelyInvokableHospital));

        CorbaTester.deliverArguments(removeNewAppointment, remotelyInvokableHospital);
        setUser("SHEA1111");
        CorbaTester.deliverArguments(removeOldAppointment, remotelyInvokableHospital);

    }

    @Test
    public void givenOldAptExistsAndNewAptExists_callingSwapAppointment100TimesAndAnotherPatientBookCancel_resultsAreConsistent()
    {
        List<String> addOldAppointment = Arrays.asList("addAppointment", "SHEA111115","Dental","1");
        List<String> addNewAppointment = Arrays.asList("addAppointment", "QUEA111115","Surgeon","1");

        List<String> bookOldAppointment = Arrays.asList("bookAppointment", "MTLP1532", "SHEA111115", "Dental");
        List<String> swapOnce = Arrays.asList("swapAppointment", "MTLP1532", "SHEA111115", "Dental" ,"QUEA111115", "Surgeon");
        List<String> swapAgain = Arrays.asList("swapAppointment", "MTLP1532" ,"QUEA111115", "Surgeon", "SHEA111115", "Dental");

        List<String> listDental = Arrays.asList("listAppointmentAvailability", "Dental");
        List<String> listSurgeon = Arrays.asList("listAppointmentAvailability", "Surgeon");

        List<String> removeOldAppointment = Arrays.asList("removeAppointment", "SHEA111115", "Dental");
        List<String> removeNewAppointment = Arrays.asList("removeAppointment", "QUEA111115", "Surgeon");

        List<String> oneBookingPatient = Arrays.asList("bookAppointment", "SHEP1535", "SHEA111115", "Dental");
        List<String> oneCancellingPatient = Arrays.asList("cancelAppointment", "SHEP1535", "SHEA111115", "Dental");

        List<String> getAppointmentScheduleSwapper = Arrays.asList("getAppointmentSchedule", "MTLP1532");
        List<String> getAppointmentScheduleGuyOne = Arrays.asList("getAppointmentSchedule", "SHEP1535");
        List<String> getAppointmentScheduleGuyTwo = Arrays.asList("getAppointmentSchedule", "SHEP1536");

        setUser("QUEA1111");
        CorbaTester.deliverArguments(removeNewAppointment, remotelyInvokableHospital);
        setUser("SHEA1111");
        CorbaTester.deliverArguments(removeOldAppointment, remotelyInvokableHospital);

        setUser("SHEA1111");
        CorbaTester.deliverArguments(addOldAppointment, remotelyInvokableHospital);
        CorbaTester.deliverArguments(bookOldAppointment, remotelyInvokableHospital);
        setUser("QUEA1111");
        CorbaTester.deliverArguments(addNewAppointment, remotelyInvokableHospital);
        setUser("MTLA1111");
        Thread t1;
        Thread t2;
        Thread t4;
        Thread t6;
        setUser("QUEA1111");
        for(int i = 0; i<100; i++)
        {
             t1 = new CorbaTester(swapOnce, remotelyInvokableHospital);
             t2 = new CorbaTester(oneBookingPatient, remotelyInvokableHospital);
             t4 = new CorbaTester(oneCancellingPatient, remotelyInvokableHospital);
             t6 = new CorbaTester(swapAgain, remotelyInvokableHospital);
            t1.start();
            t2.start();
            t4.start();
            t6.start();

            try {
                t1.join();
                t2.join();
                t4.join();
                t6.join();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        String dentalResult = CorbaTester.deliverArguments(listDental, remotelyInvokableHospital);
        String surgeonResult = CorbaTester.deliverArguments(listSurgeon, remotelyInvokableHospital);

        System.out.println("Dental after actions: " + dentalResult);
        System.out.println("Surgeon after actions: " + surgeonResult);

        assertTrue(dentalResult.contains("SHEA111115 0") || dentalResult.contains("SHEA111115 1"));
        assertTrue(surgeonResult.contains("QUEA111115 0") || surgeonResult.contains("QUEA111115 1"));

        String swapperSchedule = CorbaTester.deliverArguments(getAppointmentScheduleSwapper, remotelyInvokableHospital);
        String bookerOneSchedule = CorbaTester.deliverArguments(getAppointmentScheduleGuyOne, remotelyInvokableHospital);
        String bookerTwoSchedule = CorbaTester.deliverArguments(getAppointmentScheduleGuyTwo, remotelyInvokableHospital);

        System.out.println("Swapper now has schedule: " + swapperSchedule);
        System.out.println("Booker one now has schedule: " + bookerOneSchedule);
        System.out.println("Booker two now has schedule: " + bookerTwoSchedule);

        CorbaTester.deliverArguments(removeNewAppointment, remotelyInvokableHospital);
        setUser("SHEA1111");
        CorbaTester.deliverArguments(removeOldAppointment, remotelyInvokableHospital);

    }

}