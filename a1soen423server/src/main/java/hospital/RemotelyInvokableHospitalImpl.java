package hospital;

import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPOA;
import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType;
import hospital.database.AppointmentDetails;
import hospital.database.Database;
import javafx.util.Pair;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import logging.Logger;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.math.NumberUtils;
import udp.UDPClient;
import utility.StringConversion;

import java.util.*;
import java.util.stream.Collectors;

public class RemotelyInvokableHospitalImpl extends RemotelyInvokableHospitalPOA
{
    private final Database database = Database.getInstance();
    private static String hospitalID;
    private static final UDPClient udpClient = new UDPClient();

    public RemotelyInvokableHospitalImpl ()
    {
        super();
        //fillDatabaseWithRecords();
    }

    @Override
    public String addAppointment(String appointmentId, AppointmentType appointmentType, int capacity)
    {
        String requestLog = "addAppointment " + appointmentId + " " +
                StringConversion.getAppointmentTypeString(appointmentType) + " " + capacity;
        String outcome;
        if(appointmentId.contains(hospitalID))
            outcome = database.addAppointment(appointmentId, appointmentType, capacity);
        else {
            outcome = "FAIL";
        }
        Logger.saveLog(requestLog, outcome, hospitalID);
        return outcome;
    }

    @Override
    public String removeAppointment(String appointmentId, AppointmentType appointmentType)
    {
        String requestLog = "removeAppointment " + appointmentId + " " +
                StringConversion.getAppointmentTypeString(appointmentType);
        String outcome;
        if(appointmentId.contains(hospitalID))
            outcome = database.removeAppointment(appointmentId, appointmentType);
        else {
            outcome = "FAIL";
        }
        Logger.saveLog(requestLog, outcome, hospitalID);
        return outcome;
    }

    @Override
    public String listAppointmentAvailability(AppointmentType appointmentType)
    {
        String requestLog = "listAppointmentAvailability " + StringConversion.getAppointmentTypeString(appointmentType);
        String udpString = "listAppointment " + StringConversion.getAppointmentTypeString(appointmentType);
        String thisServer = database.listAppointment(appointmentType);
        String secondServer = udpClient.sendRequest(udpString, getOtherServerIDs().get(0));
        String thirdServer = udpClient.sendRequest(udpString, getOtherServerIDs().get(1));
        String outcome = thisServer + secondServer + thirdServer;
        Logger.saveLog(requestLog, outcome, hospitalID);
        return sortListAvailability(outcome);
    }

    @Override
    public String bookAppointment(String patientId, String appointmentId, AppointmentType appointmentType)
    {
        String requestLog = "bookAppointment " + patientId + " " + appointmentId + " " +
                StringConversion.getAppointmentTypeString(appointmentType);
        String outcome;
        long numberOfAppointmentsWeekOutsideHome = getNumberOfAppointmentsInWeek(patientId, appointmentId);
        if(numberOfAppointmentsWeekOutsideHome > 2 && !AppointmentDetails.getAppointmentHospital(appointmentId).contains(hospitalID))
        {
            outcome = "FAIL";
            Logger.saveLog(requestLog, outcome, hospitalID);
            return outcome;
        }

        boolean hasAppointmentOfThatTypeThatDay = Arrays.stream(getAppointmentSchedule(patientId).split(","))
                .filter(appointment -> appointment.contains(StringConversion.getAppointmentTypeString(appointmentType)))
                .anyMatch(item -> item.contains(appointmentId.substring(4)));

        //;Dental-APTID;APTID;Physician-APTID;APTID;Surgeon-APTID;APTID
        if(hasAppointmentOfThatTypeThatDay)
        {
            outcome = "FAIL";
            Logger.saveLog(requestLog, outcome, hospitalID);
            return outcome;
        }

        String udpString = "bookAppointment " + StringConversion.getAppointmentTypeString(appointmentType) +
                " " + appointmentId + " " + patientId;
        String appointmentHospital = AppointmentDetails.getAppointmentHospital(appointmentId);
        if(appointmentHospital.equals(hospitalID))
        {
            outcome = database.bookAppointment(appointmentType, appointmentId, patientId);
            Logger.saveLog(requestLog, outcome, hospitalID);
            return outcome;
        }
        else
        {
            outcome =  udpClient.sendRequest(udpString, appointmentHospital);
            Logger.saveLog(requestLog, outcome, hospitalID);
            return outcome;
        }
    }

    @Override
    public String getAppointmentSchedule(String patientId)
    {
        String requestLog = "getAppointmentSchedule " + patientId;
        String outcome;
        String udpString = "getAppointmentSchedule " + patientId;
        String thisServer = database.getAppointmentSchedule(patientId);
        String secondServer = udpClient.sendRequest(udpString, getOtherServerIDs().get(0));
        String thirdServer = udpClient.sendRequest(udpString, getOtherServerIDs().get(1));
        outcome =  thisServer + secondServer + thirdServer;
        Logger.saveLog(requestLog, outcome, hospitalID);
        return sortAppointmentSchedule(outcome);
    }

    @Override
    public String cancelAppointment(String patientId, String appointmentId)
    {
        String requestLog = "cancelAppointment " + patientId + " " + appointmentId;
        String outcome;
        String udpString = "cancelAppointment " + patientId + " " + appointmentId;
        String appointmentHospital = AppointmentDetails.getAppointmentHospital(appointmentId);
        if(appointmentHospital.equals(hospitalID))
        {
            outcome =  database.cancelAppointment(patientId, appointmentId);
            Logger.saveLog(requestLog, outcome, hospitalID);
            return outcome;
        }
        else
        {
            outcome = udpClient.sendRequest(udpString, appointmentHospital);
            Logger.saveLog(requestLog, outcome, hospitalID);
            return outcome;
        }
    }

    @Override
    public synchronized String swapAppointment(String patientId, String oldAppointmentId, AppointmentType oldAppointmentType,
                                                      String newAppointmentId, AppointmentType newAppointmentType)
    {
        String requestLog = "swapAppointment " + patientId + " " + oldAppointmentId + " " + oldAppointmentType + " " +
                newAppointmentId + " " + newAppointmentType;
        String result;
        String oldAppointmentHospital = AppointmentDetails.getAppointmentHospital(oldAppointmentId);

        //Cancel and hold in this hospital
        if(oldAppointmentHospital.equals(hospitalID))
        {
            result =  database.cancelAndHold(patientId, oldAppointmentId, oldAppointmentType);
            Logger.saveLog(requestLog, result, hospitalID);
        //Or cancel and hold in remote hospital
        } else {
            String udpString = "cancelAndHold " + patientId + " " + oldAppointmentId +
                    " " + StringConversion.getAppointmentTypeString(oldAppointmentType);
            result = udpClient.sendRequest(udpString, oldAppointmentHospital);
            Logger.saveLog(requestLog, result, hospitalID);
        }
        //Given successful cancel+hold Attempt to book new appointment
        if(result.contains("SUCCESS"))
        {
            result = bookAppointment(patientId, newAppointmentId, newAppointmentType);

            //If book failed rebook old appointment and unhold
            if(result.contains("FAIL"))
            {
                if(oldAppointmentHospital.equals(hospitalID))
                {
                    database.unholdAndRebook(patientId, oldAppointmentId, oldAppointmentType);
                } else {
                    String udpString = "unholdAndRebook " + patientId + " " + oldAppointmentId +
                            " " + StringConversion.getAppointmentTypeString(oldAppointmentType);
                    udpClient.sendRequest(udpString, oldAppointmentHospital);
                }
                //Otherwise unhold the old appointment
            } else {
                if(oldAppointmentHospital.equals(hospitalID))
                {
                    database.unHold(oldAppointmentId, oldAppointmentType);
                } else {
                    String udpString = "unhold " + oldAppointmentId +
                            " " + StringConversion.getAppointmentTypeString(oldAppointmentType);
                    udpClient.sendRequest(udpString, oldAppointmentHospital);
                }
            }
        }
        return result;

    }

    public static void setHospitalID(String hospitalID) {
        RemotelyInvokableHospitalImpl.hospitalID = hospitalID;
    }

    private ArrayList<String> getOtherServerIDs()
    {
        ArrayList<String> serverIDs = new ArrayList<>(Arrays.asList("MTL", "SHE", "QUE"));
        serverIDs.remove(hospitalID);
        return serverIDs;
    }

    private long getNumberOfAppointmentsInWeek(String patientId, String appointmentId)
    {
        String udpNumAppointmentInWeek = "numberOfAppointmentsInWeek " + appointmentId + " " + patientId;
        ArrayList<String> otherHospitals = getOtherServerIDs();
        String hospital0Number;
        String hospital1Number;

        if(patientFromOtherHospital(patientId))
        {
            long numberOfAppointmentsInWeekHere = database.numberOfAppointmentsInWeek(appointmentId, patientId);
            boolean id0IsHome = patientId.contains(otherHospitals.get(0));
            if(id0IsHome)
            {
                hospital0Number = udpClient.sendRequest(udpNumAppointmentInWeek, otherHospitals.get(1));
                while(!NumberUtils.isParsable(hospital0Number)) {
                    hospital0Number = udpClient.sendRequest(udpNumAppointmentInWeek, otherHospitals.get(1));
                }
                return numberOfAppointmentsInWeekHere + Integer.parseInt(hospital0Number);
            } else {
                hospital1Number = udpClient.sendRequest(udpNumAppointmentInWeek, otherHospitals.get(0));
                while(!NumberUtils.isParsable(hospital1Number)) {
                    hospital1Number = udpClient.sendRequest(udpNumAppointmentInWeek, otherHospitals.get(0));
                    return numberOfAppointmentsInWeekHere + Integer.parseInt(hospital1Number);
                }
            }
        }

        hospital0Number  = udpClient.sendRequest(udpNumAppointmentInWeek, otherHospitals.get(0));
        hospital1Number = udpClient.sendRequest(udpNumAppointmentInWeek, otherHospitals.get(1));
        while(!NumberUtils.isParsable(hospital0Number) || !NumberUtils.isParsable(hospital1Number))
        {
            hospital0Number  = udpClient.sendRequest(udpNumAppointmentInWeek, otherHospitals.get(0));
            hospital1Number = udpClient.sendRequest(udpNumAppointmentInWeek, otherHospitals.get(1));
        }
        int numInFirstHospital = Integer.parseInt(hospital0Number);
        int numInSecondHospital = Integer.parseInt(hospital1Number);
        return numInFirstHospital + numInSecondHospital;
    }

    private boolean patientFromOtherHospital(String patientID)
    {
        return !patientID.contains(hospitalID);
    }

    private void fillDatabaseWithRecords()
    {
        database.addAppointment(new AppointmentDetails(3,hospitalID + "A121212", AppointmentType.Surgeon));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "M121212", AppointmentType.Surgeon));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "E121212", AppointmentType.Surgeon));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "A121212", AppointmentType.Physician));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "A121212", AppointmentType.Physician));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "M121212", AppointmentType.Dental));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "A121212", AppointmentType.Dental));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "A101212", AppointmentType.Dental));

        database.addAppointment(new AppointmentDetails(3,hospitalID + "A111111", AppointmentType.Surgeon));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "M111111", AppointmentType.Surgeon));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "E111111", AppointmentType.Surgeon));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "A111111", AppointmentType.Physician));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "A111111", AppointmentType.Physician));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "M111111", AppointmentType.Dental));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "A111111", AppointmentType.Dental));
        database.addAppointment(new AppointmentDetails(3,hospitalID + "A101111", AppointmentType.Dental));
    }

    public static String sortListAvailability(String appointmentAvailability)
    {
        ArrayList<String> availabilityList = new ArrayList<>(Arrays.asList(appointmentAvailability.split(";")));
        availabilityList.remove(0);
        Collections.sort(availabilityList);
        return availabilityList.stream().reduce(";", (item1, item2) -> {
            if(item1.equals(";"))
                return ";" + item2;
            return item1 + ";" + item2;
        });
    }

    public static String sortAppointmentSchedule(String appointmentSchedule)
    {
        ArrayList<String> appointments = new ArrayList<>(Arrays.asList(appointmentSchedule.split(";")));
        appointments.remove(0);
        List<Pair<String,String>> aptPairs = StreamEx.of(appointments)
                .pairMap((a,b) -> new Pair<String,String>(a, b))
                .filter(item -> item.getKey().matches("(Surgeon|Physician|Dental)"))
                .collect(Collectors.toList());

        List<Pair<String,String>> dental = new ArrayList<>();
        List<Pair<String,String>>  physician = new ArrayList<>();
        List<Pair<String,String>>  surgeon = new ArrayList<>();

        aptPairs.stream()
                .forEach(item -> {
                    if(item.getKey().equals("Dental"))
                        dental.add(item);
                    if(item.getKey().equals("Surgeon"))
                        surgeon.add(item);
                    if(item.getKey().equals("Physician"))
                        physician.add(item);
                });

        String dentalString = getStringifiedSortedAppointments(AppointmentType.Dental, dental);
        String physicianString = getStringifiedSortedAppointments(AppointmentType.Physician, physician);
        String surgeonString = getStringifiedSortedAppointments(AppointmentType.Surgeon, surgeon);

        StringBuilder sb = new StringBuilder();
        sb.append(dentalString).append(physicianString).append(surgeonString);
        return sb.toString();
    }

    private static String getStringifiedSortedAppointments(AppointmentType appointmentType, List<Pair<String,String>> aptList)
    {
        return aptList.stream()
                .sorted(Comparator.comparing(Pair::getValue))
                .map(item -> ";"+StringConversion.getAppointmentTypeString(appointmentType)+";" + item.getValue())
                .reduce("", (item1, item2) -> item1+item2);
    }

}
