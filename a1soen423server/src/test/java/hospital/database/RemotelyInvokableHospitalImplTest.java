package hospital.database;

import hospital.RemotelyInvokableHospitalImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemotelyInvokableHospitalImplTest {


    @Test
    public void givenUnsortedSchedule_callingSortAppointmentSchedule_outputsSortedSchedule()
    {
        String inputString = ";Dental;MTLM201119;Physician;MTLE151119;Dental;MTLE151119;Dental;MTLM191119;Surgeon;MTLE151119;Surgeon;MTLM191119;Physician;MTLM191119;Physician;MTLM201119;Surgeon;MTLM201119";
        String expectedString = ";Dental;MTLE151119;Dental;MTLM191119;Dental;MTLM201119;Physician;MTLE151119;Physician;MTLM191119;Physician;MTLM201119;Surgeon;MTLE151119;Surgeon;MTLM191119;Surgeon;MTLM201119";
        String result = RemotelyInvokableHospitalImpl.sortAppointmentSchedule(inputString);
        assertEquals(expectedString, result);
    }

    @Test
    public void givenSingleItem_callingSortListAvailability_correctlyFormatsOutput()
    {
        String inputString = ";QUEE110619 1";
        String expectedString = ";QUEE110619 1";
        String result = RemotelyInvokableHospitalImpl.sortListAvailability(inputString);
        assertEquals(expectedString, result);
    }

    @Test
    public void givenTwoItems_callingSortListAvailability_correctlyFormatsOutput()
    {
        String inputString = ";QUEE110619 1;MTLA111111 3";
        String expectedString = ";MTLA111111 3;QUEE110619 1";
        String result = RemotelyInvokableHospitalImpl.sortListAvailability(inputString);
        assertEquals(expectedString, result);
    }

    @Test
    public void given5Items_callingSortListAvailability_correctlyFormatsOutput()
    {
        String inputString = ";SHEA111111 4;MTLA12111111 2;SHEM111111 3;QUEE110619 1;MTLA111111 3";
        String expectedString = ";MTLA111111 3;MTLA12111111 2;QUEE110619 1;SHEA111111 4;SHEM111111 3";
        String result = RemotelyInvokableHospitalImpl.sortListAvailability(inputString);
        assertEquals(expectedString, result);
    }
}
