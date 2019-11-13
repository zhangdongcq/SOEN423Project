package hospital.database;

import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DatabaseTest {

    private Database database;
    private AppointmentDetails appointmentDetails;

    @Before
    public void setup()
    {
        Database.resetDatabase();
        database = Database.getInstance();
        appointmentDetails = new AppointmentDetails(
                5, "MTLM191119", AppointmentType.Dental);
    }

    //Remove Appointment
    @Test
    public void givenEmptyDatabase_CallingRemoveAppointment_ReturnsFalse()
    {
        assertEquals("FAIL",database.removeAppointment(appointmentDetails));
    }

    @Test
    public void givenAppointmentIsAdded_CallingRemoveAppointment_ReturnsTrue()
    {
        database.addAppointment(appointmentDetails);
        assertEquals("SUCCESS", database.removeAppointment(appointmentDetails));
    }

    @Test
    public void givenAPatientInAppointment_CallingRemoveAppointment_ReturnsTrue()
    {
        appointmentDetails.addPatient("MTLP2345");
        database.addAppointment(appointmentDetails);
        assertEquals("SUCCESS",database.removeAppointment(appointmentDetails));
    }

    @Test
    public void givenAPatientInAppointment_CallingRemoveAppointment_MovesPatientToNextAvailableDaySlot()
    {
        add12Appointments();
        AppointmentDetails originalAppointment = database.getAppointmentsForType(AppointmentType.Dental).get("MTLM191119");
        AppointmentDetails expectedAppointment = database.getAppointmentsForType(AppointmentType.Dental).get("MTLE191119");
        originalAppointment.addPatient("MTLP2345");
        database.removeAppointment(originalAppointment);
        assertTrue(expectedAppointment.getPatientsBooked().contains("MTLP2345"));
        assertNull(database.getAppointmentsForType(AppointmentType.Dental).get("MTLM191119"));
    }

    @Test
    public void given10PatientsInAppointment_CallingRemoveAppointment_MovesPatientToNextAvailableSlots()
    {
        ArrayList<String> patients = new ArrayList<>(Arrays.asList("MTLP2345", "MTLP2346", "MTLP2347", "MTLP2348", "MTLP2349",
                "MTLP2350", "MTLP2351", "MTLP2352", "MTLP2353", "MTLP2354"));
        add12Appointments();
        patients.forEach(patient -> database.bookAppointment(AppointmentType.Dental, "MTLM191119", patient));
        AppointmentDetails originalAppointment = database.getAppointmentsForType(AppointmentType.Dental).get("MTLM191119");
        AppointmentDetails expectedFirstAppointment = database.getAppointmentsForType(AppointmentType.Dental).get("MTLE191119");
        AppointmentDetails expectedSecondAppointment = database.getAppointmentsForType(AppointmentType.Dental).get("MTLM201119");
        database.removeAppointment(originalAppointment);
        assertEquals(0, expectedFirstAppointment.getCapacity());
        assertEquals(5, expectedFirstAppointment.getPatientsBooked().size());
        assertEquals(0, expectedSecondAppointment.getCapacity());
        assertEquals(5, expectedSecondAppointment.getPatientsBooked().size());
    }


    //Add appointment
    @Test
    public void givenValidAppointment_callingAddAppointment_ReturnsTrue()
    {
        assertEquals("SUCCESS",
                database.addAppointment(appointmentDetails));
    }

    @Test
    public void givenTheSameAppointmentTwice_callingAddAppointment_ReturnsFalse()
    {
        assertEquals("SUCCESS", database.addAppointment(appointmentDetails));
        assertEquals("FAIL",database.addAppointment(appointmentDetails));
    }

    //List Appointment
    @Test
    public void given4DentalAppointmentsExist_callingListAppointment_returnsThoseAppointments()
    {
        add12Appointments();
        ArrayList<String>  expectedAppointments =
                new ArrayList<>(Arrays.asList("MTLE151119 5", "MTLM191119 10", "MTLE191119 5", "MTLM201119 5"));
        assertTrue(expectedAppointments.stream().allMatch(item -> database.listAppointment(AppointmentType.Dental).contains(item)));
    }

    //Book Appointment
    @Test
    public void givenPatientHasExistingAppointmentThatDay_callingBookAppointment_returnsFailure()
    {
        add12Appointments();
        assertEquals("SUCCESS",
                database.bookAppointment(AppointmentType.Dental, "MTLM191119", "MTLP2345"));
        assertEquals("FAIL",
                database.bookAppointment(AppointmentType.Dental,"MTLE191119", "MTLP2345"));
    }

    @Test
    public void givenNoAppointmentExists_callingBookAppointment_returnsFailure()
    {
        assertEquals("FAIL",
                database.bookAppointment(AppointmentType.Surgeon,"MTLE191119", "MTLP2345"));
    }

    @Test
    public void givenPatientHasExistingAppointmentThatDay_callingBookAppointmentOnAnotherType_returnsTrue()
    {
        add12Appointments();
        assertEquals("SUCCESS",
                database.bookAppointment(AppointmentType.Dental, "MTLM191119", "MTLP2345"));
        assertEquals("SUCCESS",
                database.bookAppointment(AppointmentType.Surgeon,"MTLE191119", "MTLP2345"));
    }

    @Test
    public void givenPatientHasExistingAppointment_callingBookAppointmentOnAnotherDay_returnsTrue()
    {
        add12Appointments();
        assertEquals("SUCCESS",
                database.bookAppointment(AppointmentType.Dental, "MTLM191119", "MTLP2345"));
        assertEquals("SUCCESS",
                database.bookAppointment(AppointmentType.Dental,"MTLM201119", "MTLP2345"));
    }

    //Get appointment schedule
    @Test
    public void givenNoAppointmentsExist_callingGetAppointmentSchedule_ReturnsEmptyString()
    {
        assertEquals("", database.getAppointmentSchedule("MTLP2345"));
    }

    @Test
    public void givenPatientIsNotBookedForAppointments_callingGetAppointmentSchedule_ReturnsEmptyString()
    {
        add12Appointments();
        assertEquals("", database.getAppointmentSchedule("MTLP2345"));
    }

    @Test
    public void givenPatientHasBookedAnAppointment_callingGetAppointmentSchedule_returnsThatAppointment()
    {
        add12Appointments();
        database.bookAppointment(AppointmentType.Dental, "MTLM191119", "MTLP2345");
        assertEquals(";Dental;MTLM191119", database.getAppointmentSchedule("MTLP2345"));
    }

    @Test
    public void givenTwoPatientHaveBookedAnAppointment_callingGetAppointmentSchedule_returnsOnlyThatPatientsAppointment()
    {
        add12Appointments();
        database.bookAppointment(AppointmentType.Dental, "MTLM191119", "MTLP2345");
        database.bookAppointment(AppointmentType.Dental, "MTLM191119", "MTLP2346");
        assertEquals(";Dental;MTLM191119", database.getAppointmentSchedule("MTLP2345"));
    }

    @Test
    public void givenPatientHasBookedAppointmentsAcrossManyTypes_callingGetAppointmentSchedule_returnsAllThierAppointmentsSorted()
    {
        add12Appointments();
        bookPatientAcross9Appointments("MTLP2345");
        String expectedResult = ";Dental;MTLE151119;Dental;MTLM191119;Dental;MTLM201119;Physician;" +
                "MTLE151119;Physician;MTLM191119;Physician;MTLM201119;Surgeon;MTLE151119;Surgeon;MTLM191119;Surgeon;MTLM201119";
        assertEquals(expectedResult, database.getAppointmentSchedule("MTLP2345"));
    }

    //Cancel Appointment

    @Test
    public void givenNoAppointmentsExist_callingCancelAppointment_returnsFailure()
    {
        assertEquals("FAIL",
                database.cancelAppointment("MTLP2345", "MTLM201119"));
    }

    @Test
    public void givenAppointmentsExistNoPatientAdded_callingCancelAppointment_returnsFailure()
    {
        add12Appointments();
        assertEquals("FAIL",
                database.cancelAppointment("MTLP2345", "MTLM201119"));
    }

    @Test
    public void givenPatientHasBookedAppointment_callingCancelAppointment_returnsTrue()
    {
        add12Appointments();
        database.bookAppointment(AppointmentType.Dental, "MTLM191119", "MTLP2345");
        assertEquals("SUCCESS",
                database.cancelAppointment("MTLP2345", "MTLM191119"));
    }

    @Test
    public void givenPatientHasBookedAppointment_callingCancelAppointment_removesThePatient()
    {
        add12Appointments();
        database.bookAppointment(AppointmentType.Dental, "MTLM191119", "MTLP2345");
        assertEquals(";Dental;MTLM191119", database.getAppointmentSchedule("MTLP2345"));
        database.cancelAppointment("MTLP2345", "MTLM191119");
        assertEquals("", database.getAppointmentSchedule("MTLP2345"));
    }

    //Number of appointments in week
    @Test
    public void givenNoAppointmentsInWeek_callingNumberOfAppointmentsInWeek_returns0()
    {
        add12Appointments();
        assertEquals(0, database.numberOfAppointmentsInWeek("MTLE151119", "MTLP2345"));
    }

    @Test
    public void givenOneAppointmentsInWeek_callingNumberOfAppointmentsInWeek_returns1()
    {
        add12Appointments();
        database.bookAppointment(AppointmentType.Dental, "MTLE151119", "MTLP2345");
        assertEquals(1, database.numberOfAppointmentsInWeek("MTLE151119", "MTLP2345"));
    }

    @Test
    public void givenAppointmentInEachCategory_callingNumberOfAppointmentsInWeek_returns9()
    {
        add12Appointments();
        bookPatientAcross9Appointments("MTLP2345");
        assertEquals(9, database.numberOfAppointmentsInWeek("MTLE151119", "MTLP2345"));
    }

    @Test
    public void givenTwoPatientsHaveBookedInEachCategory_callingNumberOfAppointmentsInWeek_returns9()
    {
        add12Appointments();
        bookPatientAcross9Appointments("MTLP2345");
        bookPatientAcross9Appointments("MTLP2343");
        assertEquals(9, database.numberOfAppointmentsInWeek("MTLE151119", "MTLP2345"));
    }

    @Test
    public void givenPatientHasBookedTwoAppointmentsInSeparateWeeks_callingNumberOfAppointmentsInWeek_returns1()
    {
        add12Appointments();
        database.addAppointment(new AppointmentDetails(5,  "MTLM191119", AppointmentType.Surgeon));
        database.bookAppointment(AppointmentType.Surgeon,"MTLM191119", "MTLP2345");
        database.addAppointment(new AppointmentDetails(5,  "MTLM291119", AppointmentType.Surgeon));
        database.bookAppointment(AppointmentType.Surgeon,"MTLM291119", "MTLP2345");
        assertEquals(1, database.numberOfAppointmentsInWeek("MTLE151119", "MTLP2345"));
    }

    private void add12Appointments()
    {
        ArrayList<AppointmentDetails> appointments = new ArrayList<>(Arrays.asList(
                new AppointmentDetails(5, "MTLE151119", AppointmentType.Dental),
                new AppointmentDetails(10, "MTLM191119", AppointmentType.Dental),
                new AppointmentDetails(5,  "MTLE191119", AppointmentType.Dental),
                new AppointmentDetails(5,  "MTLM201119", AppointmentType.Dental),
                new AppointmentDetails(5,  "MTLE191119", AppointmentType.Physician),
                new AppointmentDetails(5, "MTLM191119", AppointmentType.Physician),
                new AppointmentDetails(5,  "MTLE151119", AppointmentType.Physician),
                new AppointmentDetails(5,  "MTLM201119", AppointmentType.Physician),
                new AppointmentDetails(5,  "MTLM191119", AppointmentType.Surgeon),
                new AppointmentDetails(5, "MTLE191119", AppointmentType.Surgeon),
                new AppointmentDetails(5,  "MTLE151119", AppointmentType.Surgeon),
                new AppointmentDetails(5,  "MTLM201119", AppointmentType.Surgeon)));
        appointments.forEach(database::addAppointment);
    }

    private void bookPatientAcross9Appointments(String patientID)
    {
        database.bookAppointment(AppointmentType.Dental, "MTLM191119", patientID);
        database.bookAppointment(AppointmentType.Dental, "MTLE151119", patientID);
        database.bookAppointment(AppointmentType.Dental, "MTLM201119", patientID);

        database.bookAppointment(AppointmentType.Physician, "MTLM191119", patientID);
        database.bookAppointment(AppointmentType.Physician, "MTLE151119", patientID);
        database.bookAppointment(AppointmentType.Physician, "MTLM201119", patientID);

        database.bookAppointment(AppointmentType.Surgeon, "MTLM191119", patientID);
        database.bookAppointment(AppointmentType.Surgeon, "MTLE151119", patientID);
        database.bookAppointment(AppointmentType.Surgeon, "MTLM201119", patientID);
    }

}
