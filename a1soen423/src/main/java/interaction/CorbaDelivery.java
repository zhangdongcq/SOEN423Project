package interaction;

import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;
import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType;
import utility.StringConversion;

import java.util.ArrayList;
import java.util.List;

public class CorbaDelivery {

    public static String deliverCorbaRequest(List<String> deliveryArguments, RemotelyInvokableHospital remotelyInvokableHospital)
    {
        String option = deliveryArguments.get(0);
        switch (option)
        {
            case "logout":
                return "Logging out user";
            case "bookAppointment":
                return remotelyInvokableHospital.bookAppointment(
                        deliveryArguments.get(1), deliveryArguments.get(2),
                        StringConversion.getAppointmentType(deliveryArguments.get(3)));
            case "getAppointmentSchedule":
                return remotelyInvokableHospital.getAppointmentSchedule(
                        deliveryArguments.get(1));
            case "cancelAppointment":
                return remotelyInvokableHospital.cancelAppointment(
                        deliveryArguments.get(1), deliveryArguments.get(2));
            case "addAppointment":
                return remotelyInvokableHospital.addAppointment(
                        deliveryArguments.get(1), StringConversion.getAppointmentType(deliveryArguments.get(2)),
                        Integer.parseInt(deliveryArguments.get(3)));
            case "removeAppointment":
                return remotelyInvokableHospital.removeAppointment(
                        deliveryArguments.get(1), StringConversion.getAppointmentType(deliveryArguments.get(2))
                );
            case "listAppointmentAvailability":
                return remotelyInvokableHospital.listAppointmentAvailability(
                        StringConversion.getAppointmentType(deliveryArguments.get(1)));
            case "swapAppointment":
                    return remotelyInvokableHospital.swapAppointment(deliveryArguments.get(1), deliveryArguments.get(2),
                            StringConversion.getAppointmentType(deliveryArguments.get(3)), deliveryArguments.get(4),
                            StringConversion.getAppointmentType(deliveryArguments.get(5)));
            default:
                return "Error: unknown RMI method";
        }
    }

}
