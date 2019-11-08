package utility;

import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType;

public class StringConversion {

    public static AppointmentType getAppointmentType(String appointmentString)
    {
        switch (appointmentString)
        {
            case "Surgeon":
                return AppointmentType.Surgeon;
            case "Physician":
                return AppointmentType.Physician;
            case "Dental":
                return AppointmentType.Dental;
            default:
                return null;
        }
    }
}
