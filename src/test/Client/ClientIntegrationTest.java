package test.Client;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientIntegrationTest {

    @Test
    public void givenPatientBooks3AppointmentsInAWeekInOtherHospitals_callingSwapAppointment_returnsFail()
    {
        String user = "SHEA1111";
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA121236","Dental");
        List<String> removeAppointment2 = Arrays.asList("removeAppointment", "SHEA121236","Physician");
        List<String> removeAppointment3 = Arrays.asList("removeAppointment", "SHEA121236","Surgeon");
        List<String> removeAppointment4 = Arrays.asList("removeAppointment", "QUEA101236","Dental");
        List<String> removeAppointment5 = Arrays.asList("removeAppointment", "QUEA111119" ,"Dental");

        ClientTester.deliverArguments(user, removeAppointment);
        ClientTester.deliverArguments(user,removeAppointment2);
        ClientTester.deliverArguments(user, removeAppointment);
        user ="QUEA1111";
        ClientTester.deliverArguments(user,removeAppointment4);
        ClientTester.deliverArguments(user,removeAppointment5);
        user ="SHEA1111";

        List<String> addAppointment = Arrays.asList("addAppointment", "SHEA121236","Dental","1");
        List<String> addAppointment2 = Arrays.asList("addAppointment", "SHEA121236","Physician","1");
        List<String> addAppointment3 = Arrays.asList("addAppointment", "SHEA121236","Surgeon","1");
        List<String> addAppointment4 = Arrays.asList("addAppointment", "QUEA101236","Dental","1");
        List<String> addAppointment5 = Arrays.asList("addAppointment", "QUEA111119","Dental","1");

        ClientTester.deliverArguments(user,addAppointment);
        ClientTester.deliverArguments(user,addAppointment2);
        ClientTester.deliverArguments(user,addAppointment3);
        user = "QUEA1111";
        ClientTester.deliverArguments(user,addAppointment4);
        ClientTester.deliverArguments(user,addAppointment5);
        user = "SHEA1111";

        List<String> bookAppointment1 = Arrays.asList("bookAppointment","MTLP1222", "SHEA121236","Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "MTLP1222","SHEA121236","Physician");
        List<String> bookAppointment3 = Arrays.asList("bookAppointment","MTLP1222", "SHEA121236","Surgeon");
        List<String> bookAppointment4 = Arrays.asList("bookAppointment","MTLP1222", "QUEA111119","Dental");
        List<String> swapAppointment = Arrays.asList("swapAppointment","MTLP1222", "QUEA111119","Dental", "QUEA101236","Dental");

        String result = ClientTester.deliverArguments(user,bookAppointment1);
        assertEquals("SUCCESS", result);
        result = ClientTester.deliverArguments(user,bookAppointment2);
        assertEquals("SUCCESS", result);
        result = ClientTester.deliverArguments(user,bookAppointment3);
        assertEquals("SUCCESS", result);
        result = ClientTester.deliverArguments(user,bookAppointment4);
        assertEquals("SUCCESS", result);
        result = ClientTester.deliverArguments(user,swapAppointment);
        assertEquals("FAIL", result);

        ClientTester.deliverArguments(user,removeAppointment);
        ClientTester.deliverArguments(user,removeAppointment2);
        ClientTester.deliverArguments(user,removeAppointment3);
        user = "QUEA1111";
        ClientTester.deliverArguments(user,removeAppointment4);

    }


    @Test
    public void givenAppointmentFull_callingBookAppointment_returnsFailure()
    {
        String user = "SHEA1111";
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA111136","Dental");
        ClientTester.deliverArguments(user,removeAppointment);
        List<String> addAppointment = Arrays.asList("addAppointment", "SHEA111136","Dental","1");
        List<String> bookAppointment1 = Arrays.asList("bookAppointment", "MTLP1532", "SHEA111136", "Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "SHEP1532", "SHEA111136", "Dental");


        ClientTester.deliverArguments(user,addAppointment);
        ClientTester.deliverArguments(user,bookAppointment1);
        String result = ClientTester.deliverArguments(user,bookAppointment2);
        assertEquals("FAIL", result);
        ClientTester.deliverArguments(user,removeAppointment);
    }

    @Test
    public void givenPatientBooksAnAppointment_bookingSameTypeDay_returnsFail()
    {
        String user = "MTLA1111";
        List<String> removeAppointment = Arrays.asList("removeAppointment", "MTLE121236","Dental");
        List<String> removeAppointment2 = Arrays.asList("removeAppointment", "SHEA121236","Dental");
        List<String> removeAppointment3 = Arrays.asList("removeAppointment", "MTLE121236","Dental");
        List<String> removeAppointment4 = Arrays.asList("removeAppointment", "MTLE121236","Surgeon");

        ClientTester.deliverArguments(user,removeAppointment);
        ClientTester.deliverArguments(user,removeAppointment3);
        ClientTester.deliverArguments(user,removeAppointment4);
        user = "SHEA1111";
        ClientTester.deliverArguments(user,removeAppointment2);

        List<String> addAppointment = Arrays.asList("addAppointment", "MTLE121236","Dental","1");
        List<String> addAppointment2 = Arrays.asList("addAppointment", "SHEA121236","Dental","1");
        List<String> addAppointment3 = Arrays.asList("addAppointment", "MTLE121236","Dental","1");
        List<String> addAppointment4 = Arrays.asList("addAppointment", "MTLE121236","Surgeon","1");

        List<String> bookAppointment1 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "MTLP1234", "SHEA121236", "Dental");
        List<String> bookAppointment3 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Dental");
        List<String> bookAppointment4 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Surgeon");


        ClientTester.deliverArguments(user,addAppointment2);
        user = "MTLA1111";
        ClientTester.deliverArguments(user,addAppointment);
        ClientTester.deliverArguments(user,addAppointment3);
        ClientTester.deliverArguments(user,addAppointment4);


        assertEquals("SUCCESS", ClientTester.deliverArguments(user,bookAppointment1));
        assertEquals("FAIL",
                ClientTester.deliverArguments(user,bookAppointment2));
        assertEquals("FAIL",
                ClientTester.deliverArguments(user,bookAppointment3));
        assertEquals("SUCCESS",
                ClientTester.deliverArguments(user,bookAppointment4));

        ClientTester.deliverArguments(user,removeAppointment);
        ClientTester.deliverArguments(user,removeAppointment3);
        ClientTester.deliverArguments(user,removeAppointment4);
        user = "SHEA1111";
        ClientTester.deliverArguments(user,removeAppointment2);
    }

    @Test
    public void givenAppointmentsBookedInDifferentCategoriesandPlaces_callingGetAppointmentSchedule_returnsThem()
    {
        String user = "MTLA1111";
        List<String> removeAppointment = Arrays.asList("removeAppointment", "MTLE121236","Dental");
        List<String> removeAppointment2 = Arrays.asList("removeAppointment", "SHEA121237","Dental");
        List<String> removeAppointment3 = Arrays.asList("removeAppointment", "MTLE121236","Physician");
        List<String> removeAppointment4 = Arrays.asList("removeAppointment", "MTLE121236","Surgeon");

        ClientTester.deliverArguments(user,removeAppointment);
        ClientTester.deliverArguments(user,removeAppointment3);
        ClientTester.deliverArguments(user,removeAppointment4);
        user = "SHEA1111";
        ClientTester.deliverArguments(user,removeAppointment2);

        List<String> addAppointment = Arrays.asList("addAppointment", "MTLE121236","Dental","1");
        List<String> addAppointment2 = Arrays.asList("addAppointment", "SHEA121237","Dental","1");
        List<String> addAppointment3 = Arrays.asList("addAppointment", "MTLE121236","Physician","1");
        List<String> addAppointment4 = Arrays.asList("addAppointment", "MTLE121236","Surgeon","1");

        ClientTester.deliverArguments(user,addAppointment2);
        user = "MTLA1111";
        ClientTester.deliverArguments(user,addAppointment);
        ClientTester.deliverArguments(user,addAppointment3);
        ClientTester.deliverArguments(user,addAppointment4);

        List<String> bookAppointment1 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "MTLP1234", "SHEA121237", "Dental");
        List<String> bookAppointment3 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Physician");
        List<String> bookAppointment4 = Arrays.asList("bookAppointment", "MTLP1234", "MTLE121236", "Surgeon");

        ClientTester.deliverArguments(user,bookAppointment1);
        ClientTester.deliverArguments(user,bookAppointment2);
        ClientTester.deliverArguments(user,bookAppointment3);
        ClientTester.deliverArguments(user,bookAppointment4);

        List<String> listSchedule = Arrays.asList("getAppointmentSchedule", "MTLP1234");
        String result = ClientTester.deliverArguments(user,listSchedule);
        assertEquals(";Dental;MTLE121236;Dental;SHEA121237;Physician;MTLE121236;Surgeon;MTLE121236", result);

        ClientTester.deliverArguments(user,removeAppointment);
        ClientTester.deliverArguments(user,removeAppointment3);
        ClientTester.deliverArguments(user,removeAppointment4);
        user = "SHEA1111";
        ClientTester.deliverArguments(user,removeAppointment2);
    }

    @Test
    public void givenAdminOfAnotherServer_callingAddRemoveAppointmentReturnsFailure()
    {
        String user = "SHEA1234";
        String result;
        List<String> addAppointment = Arrays.asList("addAppointment","MTLE121236","Dental","1");
        result = ClientTester.deliverArguments(user,addAppointment);
        assertEquals("FAIL", result);
        List<String> removeAppointment = Arrays.asList("removeAppointment", "MTLE121236","Dental");
        result = ClientTester.deliverArguments(user,removeAppointment);
        assertEquals("FAIL",result );
    }


    @Test
    public void givenPatientBooks3AppointmentsInAWeekInOtherHospitals_callingBookOnAForth_returnsFail()
    {
        String user = "SHEA1111";
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA121236","Dental");
        List<String> removeAppointment2 = Arrays.asList("removeAppointment", "SHEA121236","Physician");
        List<String> removeAppointment3 = Arrays.asList("removeAppointment", "SHEA121236","Surgeon");
        List<String> removeAppointment4 = Arrays.asList("removeAppointment", "QUEA101236","Dental");

        ClientTester.deliverArguments(user,removeAppointment);
        ClientTester.deliverArguments(user,removeAppointment2);
        ClientTester.deliverArguments(user,removeAppointment3);
        user = "QUEA1111";
        ClientTester.deliverArguments(user,removeAppointment4);
        user = "SHEA1111";

        List<String> addAppointment = Arrays.asList("addAppointment", "SHEA121236","Dental","1");
        List<String> addAppointment2 = Arrays.asList("addAppointment", "SHEA121236","Physician","1");
        List<String> addAppointment3 = Arrays.asList("addAppointment", "SHEA121236","Surgeon","1");
        List<String> addAppointment4 = Arrays.asList("addAppointment", "QUEA101236","Dental","1");

        ClientTester.deliverArguments(user,addAppointment);
        ClientTester.deliverArguments(user,addAppointment2);
        ClientTester.deliverArguments(user,addAppointment3);
        user = "QUEA1111";
        ClientTester.deliverArguments(user,addAppointment4);
        user = "SHEA1111";

        List<String> bookAppointment1 = Arrays.asList("bookAppointment","MTLP1222", "SHEA121236","Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "MTLP1222","SHEA121236","Physician");
        List<String> bookAppointment3 = Arrays.asList("bookAppointment","MTLP1222", "SHEA121236","Surgeon");
        List<String> bookAppointment4 = Arrays.asList("bookAppointment","MTLP1222", "QUEA101236","Dental");

        String result = ClientTester.deliverArguments(user,bookAppointment1);
        assertEquals("SUCCESS", result);
        result = ClientTester.deliverArguments(user,bookAppointment2);
        assertEquals("SUCCESS", result);
        result = ClientTester.deliverArguments(user,bookAppointment3);
        assertEquals("SUCCESS", result);
        result = ClientTester.deliverArguments(user,bookAppointment4);
        assertEquals("FAIL", result);

        ClientTester.deliverArguments(user,removeAppointment);
        ClientTester.deliverArguments(user,removeAppointment2);
        ClientTester.deliverArguments(user,removeAppointment3);
        user = "QUEA1111";
        ClientTester.deliverArguments(user,removeAppointment4);
    }

    @Test
    public void givenPatientsHaveBookedAndFutureAppointmentsExist_callingRemoveAppointmentMovesThemToTheNewAppointment()
    {
        String user = "SHEA1111";
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA101236","Dental");
        List<String> removeAppointment2 = Arrays.asList("removeAppointment", "SHEA121237","Dental");

        ClientTester.deliverArguments(user,removeAppointment);
        ClientTester.deliverArguments(user,removeAppointment2);

        List<String> addAppointment = Arrays.asList("addAppointment", "SHEA101236","Dental","2");
        List<String> addAppointment2 = Arrays.asList("addAppointment", "SHEA121237","Dental","2");

        ClientTester.deliverArguments(user,addAppointment);
        ClientTester.deliverArguments(user,addAppointment2);

        List<String> bookAppointment1 = Arrays.asList("bookAppointment","MTLP1233", "SHEA101236","Dental");
        List<String> bookAppointment2 = Arrays.asList("bookAppointment", "MTLP1234","SHEA101236","Dental");
        ClientTester.deliverArguments(user,bookAppointment1);
        ClientTester.deliverArguments(user,bookAppointment2);

        List<String> showAppointmentSchedule = Arrays.asList("getAppointmentSchedule", "MTLP1233");
        List<String> showAppointmentSchedule2 = Arrays.asList("getAppointmentSchedule", "MTLP1234");

        String result = ClientTester.deliverArguments(user,showAppointmentSchedule);
        assertEquals(";Dental;SHEA101236", result);
        result = ClientTester.deliverArguments(user,showAppointmentSchedule2);
        assertEquals(";Dental;SHEA101236", result);

        result = ClientTester.deliverArguments(user,removeAppointment);
        assertEquals("SUCCESS", result);

        result = ClientTester.deliverArguments(user,showAppointmentSchedule);
        assertEquals(";Dental;SHEA121237", result);

        result = ClientTester.deliverArguments(user,showAppointmentSchedule2);
        assertEquals(";Dental;SHEA121237", result);

        ClientTester.deliverArguments(user,removeAppointment);
        ClientTester.deliverArguments(user,removeAppointment2);
    }

    @Test
    public void givenPatientBooks_callingListAppointmentAvailability_returnsAReducedValue()
    {
        String user = "SHEA1111";
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA101236","Dental");

        ClientTester.deliverArguments(user,removeAppointment);

        List<String> addAppointment = Arrays.asList("addAppointment", "SHEA101236","Dental","2");
        ClientTester.deliverArguments(user,addAppointment);

        List<String> listAppointmentAvailability = Arrays.asList("listAppointmentAvailability","Dental");
        String result = ClientTester.deliverArguments(user,listAppointmentAvailability);

        assertTrue(result.contains("SHEA101236 2"));
        List<String> bookAppointment1 = Arrays.asList("bookAppointment","MTLP1233", "SHEA101236","Dental");
        ClientTester.deliverArguments(user,bookAppointment1);
        result = ClientTester.deliverArguments(user,listAppointmentAvailability);
        assertTrue(result.contains("SHEA101236 1"));

        ClientTester.deliverArguments(user,removeAppointment);
    }

    @Test
    public void givenOldAptDoesNotExist_callingSwapAppointment_doesNotBookNewApt(){
        String user = "SHEA1111";
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA111115", "Dental");
        ClientTester.deliverArguments(user,removeAppointment);
        List<String> newAppointment = Arrays.asList("addAppointment", "SHEA111115","Dental","1");
        List<String> swapAppointment = Arrays.asList("swapAppointment", "MTLP1532","QUEA111115", "Dental", "SHEA111115", "Surgeon");
        List<String> getAppointmentSchedule = Arrays.asList("getAppointmentSchedule", "MTLP1532");

        ClientTester.deliverArguments(user, newAppointment);

        assertEquals("FAIL", ClientTester.deliverArguments(user,swapAppointment));
        assertEquals("", ClientTester.deliverArguments(user,getAppointmentSchedule));

        ClientTester.deliverArguments(user,removeAppointment);
    }

    @Test
    public void givenNewAptIsNotAvailable_callingSwapAppointment_doesNotCancelOldAptOrBookNewOne()
    {
        String user = "SHEA1111";
        List<String> removeAppointment = Arrays.asList("removeAppointment", "SHEA111115", "Dental");
        ClientTester.deliverArguments(user,removeAppointment);
        List<String> addOldAppointment = Arrays.asList("addAppointment", "SHEA111115","Dental","1");
        List<String> bookOldAppointment = Arrays.asList("bookAppointment", "MTLP1532", "SHEA111115", "Dental");
        List<String> swapAppointment = Arrays.asList("swapAppointment", "MTLP1532", "SHEA111115", "Surgeon" ,"QUEA111115", "Dental");
        List<String> getAppointmentSchedule = Arrays.asList("getAppointmentSchedule", "MTLP1532");

        ClientTester.deliverArguments(user,addOldAppointment);
        ClientTester.deliverArguments(user,bookOldAppointment);

        assertEquals("FAIL",ClientTester.deliverArguments(user,swapAppointment));

        assertEquals(";Dental;SHEA111115", ClientTester.deliverArguments(user,getAppointmentSchedule));

        ClientTester.deliverArguments(user,removeAppointment);
    }

    @Test
    public void givenNewAptExistsAndOldAptExists_callingSwapAppointment_swapsTheAppointments()
    {
        List<String> removeOldAppointment = Arrays.asList("removeAppointment", "SHEA111115", "Dental");
        List<String> removeNewAppointment = Arrays.asList("removeAppointment", "QUEA111115", "Surgeon");

        String user ="QUEA1111";
        ClientTester.deliverArguments(user,removeNewAppointment);
        user ="SHEA1111";
        ClientTester.deliverArguments(user, removeOldAppointment);

        List<String> addOldAppointment = Arrays.asList("addAppointment", "SHEA111115","Dental","1");
        List<String> addNewAppointment = Arrays.asList("addAppointment", "QUEA111115","Surgeon","1");
        List<String> bookOldAppointment = Arrays.asList("bookAppointment", "MTLP1532", "SHEA111115", "Dental");
        List<String> swapAppointment = Arrays.asList("swapAppointment", "MTLP1532", "SHEA111115", "Dental" ,"QUEA111115", "Surgeon");
        List<String> getAppointmentSchedule = Arrays.asList("getAppointmentSchedule", "MTLP1532");

        user = "SHEA1111";
        ClientTester.deliverArguments(user,addOldAppointment);
        ClientTester.deliverArguments(user,bookOldAppointment);
        user = "QUEA1111";
        ClientTester.deliverArguments(user,addNewAppointment);

        assertEquals("SUCCESS",ClientTester.deliverArguments(user,swapAppointment));

        assertEquals(";Surgeon;QUEA111115", ClientTester.deliverArguments(user,getAppointmentSchedule));

        ClientTester.deliverArguments(user,removeNewAppointment);
        user = "SHEA1111";
        ClientTester.deliverArguments(user,removeOldAppointment);
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

        String user = "QUEA1111";
        ClientTester.deliverArguments(user, removeNewAppointment);
        user = "SHEA1111";
        ClientTester.deliverArguments(user, removeOldAppointment);

        user = "SHEA1111";
        ClientTester.deliverArguments(user, addOldAppointment);
        ClientTester.deliverArguments(user, bookOldAppointment);
        user = "QUEA1111";
        ClientTester.deliverArguments(user, addNewAppointment);
        user = "MTLA1111";
        Thread t1;
        Thread t2;
        Thread t4;
        Thread t6;
        user = "QUEA1111";
        for(int i = 0; i<100; i++)
        {
            t1 = new ClientTester(user, swapOnce);
            t2 = new ClientTester(user, oneBookingPatient);
            t4 = new ClientTester(user, oneCancellingPatient);
            t6 = new ClientTester(user, swapAgain);
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

        String dentalResult = ClientTester.deliverArguments(user, listDental);
        String surgeonResult = ClientTester.deliverArguments(user, listSurgeon);

        System.out.println("Dental after actions: " + dentalResult);
        System.out.println("Surgeon after actions: " + surgeonResult);

        assertTrue(dentalResult.contains("SHEA111115 0") || dentalResult.contains("SHEA111115 1"));
        assertTrue(surgeonResult.contains("QUEA111115 0") || surgeonResult.contains("QUEA111115 1"));

        String swapperSchedule = ClientTester.deliverArguments(user, getAppointmentScheduleSwapper);
        String bookerOneSchedule = ClientTester.deliverArguments(user, getAppointmentScheduleGuyOne);
        String bookerTwoSchedule = ClientTester.deliverArguments(user, getAppointmentScheduleGuyTwo);

        System.out.println("Swapper now has schedule: " + swapperSchedule);
        System.out.println("Booker one now has schedule: " + bookerOneSchedule);
        System.out.println("Booker two now has schedule: " + bookerTwoSchedule);

        ClientTester.deliverArguments(user, removeNewAppointment);
        user = "SHEA1111";
        ClientTester.deliverArguments(user, removeOldAppointment);

    }

}
