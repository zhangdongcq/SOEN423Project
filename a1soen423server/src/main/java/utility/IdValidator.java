package utility;

import hospital.database.AppointmentDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class IdValidator {

    public static boolean isValidAdminAppointment(AppointmentDetails appointmentDetails)
    {
        String appointmentID = appointmentDetails.getAppointmentID();
        return isValidAppointmentID(appointmentID);
    }

    public static boolean isValidUserID(String userID)
    {
        return !Objects.isNull(userID) && userID.matches("^(SHE|MTL|QUE){1}(A|P){1}[0-9]{4}$");
    }

    public static boolean isPatientID(String userID)
    {
        return userID.contains("P");
    }

    public static boolean isValidAppointmentID(String appointmentID)
    {
        if(Objects.isNull(appointmentID))
            return false;

        boolean isLocationDayTimeCorrect = appointmentID.matches("^(SHE|MTL|QUE){1}(A|M|E){1}[0-9]*$");
        String dateString = appointmentID.substring(4);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        try {LocalDate.parse(dateString, formatter); } catch (DateTimeParseException e)
        {
            return false;
        }
        return isLocationDayTimeCorrect;
    }

    public static boolean isValidHospitalID(String hospitalID)
    {
        return Objects.nonNull(hospitalID) && hospitalID.matches("^(SHE|MTL|QUE){1}$");
    }



}
