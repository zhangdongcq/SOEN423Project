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
}
