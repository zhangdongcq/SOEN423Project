package interaction;

import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;
import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType;
import utility.StringConversion;

import java.util.ArrayList;
import java.util.List;

public class CorbaDelivery {

    public static String deliverCorbaRequest(List<String> deliveryArguments, RemotelyInvokableHospital remotelyInvokableHospital)
    {
        String option = deliveryArguments.get(4);
        switch (option)
        {
            case "logout":
                return "Logging out user";
            case "bookAppointment":
                return remotelyInvokableHospital.bookAppointment(
                        deliveryArguments.get(5), deliveryArguments.get(6),
                        StringConversion.getAppointmentType(deliveryArguments.get(7)));
            case "getAppointmentSchedule":
            	String resultSch =   remotelyInvokableHospital.getAppointmentSchedule(
                        deliveryArguments.get(5));
            	if(resultSch.isEmpty())
            		return ";";
            	return resultSch;
            case "cancelAppointment":
                return remotelyInvokableHospital.cancelAppointment(
                        deliveryArguments.get(5), deliveryArguments.get(6));
            case "addAppointment":
                return remotelyInvokableHospital.addAppointment(
                        deliveryArguments.get(5), StringConversion.getAppointmentType(deliveryArguments.get(6)),
                        Integer.parseInt(deliveryArguments.get(7)));
            case "removeAppointment":
                return remotelyInvokableHospital.removeAppointment(
                        deliveryArguments.get(5), StringConversion.getAppointmentType(deliveryArguments.get(6))
                );
            case "listAppointmentAvailability":
            	String result =  remotelyInvokableHospital.listAppointmentAvailability(
                        StringConversion.getAppointmentType(deliveryArguments.get(5)));
            	if(result.isEmpty())
            		return ";";
            	return result;
            case "swapAppointment":
                    return remotelyInvokableHospital.swapAppointment(deliveryArguments.get(5), deliveryArguments.get(6),
                            StringConversion.getAppointmentType(deliveryArguments.get(7)), deliveryArguments.get(8),
                            StringConversion.getAppointmentType(deliveryArguments.get(9)));
            default:
                return "Error: unknown method";
        }
    }

}
