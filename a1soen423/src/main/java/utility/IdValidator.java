package utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class IdValidator {

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
        if(Objects.isNull(appointmentID) || appointmentID.length() != 10)
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

    public static boolean isValidAppointmentType(String testString) {

        return testString.equals("Physician") || testString.equals("Surgeon") || testString.equals("Dental");
    }



}
