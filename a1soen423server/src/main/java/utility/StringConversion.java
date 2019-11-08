package utility;

import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType;

public class StringConversion {

    private static final int SHE_UDP_PORTNUMBER = 3001;
    private static final int MTL_UDP_PORTNUMBER = 4001;
    private static final int QUE_UDP_PORTNUMBER = 5001;

    public static int getUDPPortNumber(String hospitalID)
    {
        switch (hospitalID)
        {
            case "MTL":
                return MTL_UDP_PORTNUMBER;
            case "SHE":
                return SHE_UDP_PORTNUMBER;
            case "QUE":
                return QUE_UDP_PORTNUMBER;
        }
        return 0; //Should not be possible
    }

    public static String getAppointmentTypeString(AppointmentType appointmentType)
    {
        int thisType = appointmentType.value();
        switch (thisType)
        {
            case 0:
                return "Physician";
            case 1:
                return "Surgeon";
            case 2:
                return "Dental";
            default:
                return "Error: Unknown Appointment Type";
        }
    }
}
