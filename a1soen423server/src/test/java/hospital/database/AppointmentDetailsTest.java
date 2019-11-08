package hospital.database;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class AppointmentDetailsTest {

    //Get appointment date
    @Test
    public void givenValidAppointment_callingGetAppointmentDate_returnsTheCorrectDate()
    {
        LocalDate date = AppointmentDetails.getAppointmentDate("MTLA251120");
        assertEquals(LocalDate.of(2020, 11, 25), date);
    }

    @Test
    public void givenAppointmentDayslots_callingGetAppointmentDaySlot_returnsAppropriateInteger()
    {
        assertEquals(1, AppointmentDetails.getAppointmentDaySlotNumber("MTLM251120"));
        assertEquals(2, AppointmentDetails.getAppointmentDaySlotNumber("MTLA251120"));
        assertEquals(3, AppointmentDetails.getAppointmentDaySlotNumber("MTLE251120"));
    }

    //Get appointment week
    @Test
    public void givenAppointmentIsAtStartOfTime_callingGetAppointmentWeek_returns1()
    {
        assertEquals(1, AppointmentDetails.getAppointmentWeek("MTLM010100"));
    }

    @Test
    public void givenAppointmentIsAtEndOfFirstYear_callingGetAppointmentWeek_returns48()
    {
        assertEquals(48, AppointmentDetails.getAppointmentWeek("MTLM311200"));
    }

    @Test
    public void givenAppointmentIsYearAfterStartOfTime_callingGetAppointmentWeek_returns48()
    {
        assertEquals(49, AppointmentDetails.getAppointmentWeek("MTLM010101"));
    }

    @Test
    public void givenAppointmentIsJune12thAfterStartOfTime_callingGetAppointmentWeek_returns26()
    {
        assertEquals(22, AppointmentDetails.getAppointmentWeek("MTLM120600"));
    }

    //Get Appointment Hospital
    @Test
    public void givenMTL_callingGetAppointmentHospital_returnsMTL()
    {
        assertEquals("MTL", AppointmentDetails.getAppointmentHospital("MTLM120600"));
    }


}
