package utility;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class IdValidatorTest {


    //USER ID VALIDATION
    @Test
    public void givenValidAdminUserID_CallingValidateUserID_returnsTrue()
    {
        assertTrue(IdValidator.isValidUserID("MTLA3456"));
    }

    @Test
    public void givenValidPatientUserID_CallingValidateUserID_returnsTrue()
    {
        assertTrue(IdValidator.isValidUserID("MTLP3456"));
    }

    @Test
    public void givenValidSheID_CallingValidateUserID_returnsTrue()
    {
        assertTrue(IdValidator.isValidUserID("SHEP3456"));
    }

    @Test
    public void givenValidQueID_CallingValidateUserID_returnsTrue()
    {
        assertTrue(IdValidator.isValidUserID("QUEP3456"));
    }

    @Test
    public void givenInvalidLocationID_CallingValidateUserID_returnsFalse()
    {
        assertFalse(IdValidator.isValidUserID("UQEP3456"));
    }

    @Test
    public void givenTooLongUserNumberID_CallingValidateUserID_returnsFalse()
    {
        assertFalse(IdValidator.isValidUserID("QUEP34566"));
    }

    @Test
    public void givenTooShortUserNumberID_CallingValidateUserID_returnsFalse()
    {
        assertFalse(IdValidator.isValidUserID("QUEP345"));
    }

    @Test
    public void givenInvalidUserTYPE_CallingValidateUserID_returnsFalse()
    {
        assertFalse(IdValidator.isValidUserID("QUEF3456"));
    }

    @Test
    public void givenNull_CallingValidateUserID_returnsFalse()
    {
        assertFalse(IdValidator.isValidUserID(null));
    }

    //APPOINTMENT ID VALIDATION
    @Test
    public void givenValidAfternoonAppointmentID_CallingValidateAppointment_returnsTrue()
    {
        assertTrue(IdValidator.isValidAppointmentID("MTLA191019"));
    }

    @Test
    public void givenValidEveningAppointmentID_CallingValidateAppointment_returnsTrue()
    {
        assertTrue(IdValidator.isValidAppointmentID("MTLE191019"));
    }

    @Test
    public void givenValidMorningAppointmentID_CallingValidateAppointment_returnsTrue()
    {
        assertTrue(IdValidator.isValidAppointmentID("MTLM191019"));
    }

    @Test
    public void givenInvalidDaySlotAppointmentID_CallingValidateAppointment_returnsFalse()
    {
        assertFalse(IdValidator.isValidAppointmentID("MTLP191019"));
    }

    @Test
    public void givenValidMtlAppointmentID_CallingValidateAppointment_returnsTrue()
    {
        assertTrue(IdValidator.isValidAppointmentID("MTLA191019"));
    }

    @Test
    public void givenValidSheAppointmentID_CallingValidateAppointment_returnsTrue()
    {
        assertTrue(IdValidator.isValidAppointmentID("SHEA191019"));
    }

    @Test
    public void givenValidQueAppointmentID_CallingValidateAppointment_returnsTrue()
    {
        assertTrue(IdValidator.isValidAppointmentID("QUEA191019"));
    }

    @Test
    public void givenInvalidLocationID_CallingValidateAppointment_returnsFalse()
    {
        assertFalse(IdValidator.isValidAppointmentID("UEHA191019"));
    }

    @Test
    public void givenImpossibleDate_CallingValidateAppointment_returnsFalse()
    {
        assertFalse(IdValidator.isValidAppointmentID("QUEA401019"));
    }

    @Test
    public void givenNull_CallingValidateAppointment_returnsFalse()
    {
        assertFalse(IdValidator.isValidAppointmentID(null));
    }

    //Hospital ID utility
    @Test
    public void givenNull_callingIsValidHospitalId_returnsFalse()
    {
        assertFalse(IdValidator.isValidHospitalID(null));
    }

    @Test
    public void givenValidMTLID_callingIsValidHospitalID_returnsTrue()
    {
        assertTrue(IdValidator.isValidHospitalID("MTL"));
    }

}
