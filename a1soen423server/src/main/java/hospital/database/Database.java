package hospital.database;


import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType;
import utility.IdValidator;
import utility.StringConversion;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Database  {

    private HashMap<AppointmentType, HashMap<String, AppointmentDetails>> database;
    private static final Lock lock = new ReentrantLock();
    private static Database ONLY_INSTANCE = null;

    private Database(){
        database = new HashMap<>();
        database.put(AppointmentType.Dental, new HashMap<>());
        database.put(AppointmentType.Physician, new HashMap<>());
        database.put(AppointmentType.Surgeon, new HashMap<>());
    }

    public synchronized static Database getInstance()
    {
        if(ONLY_INSTANCE == null)
        {
            ONLY_INSTANCE = new Database();
        }
        return ONLY_INSTANCE;
    }

    /**
     *
     * @param appointmentDetails
     * @return False if the appointment cannot be added. E.g already exists
     */
    public String addAppointment(AppointmentDetails appointmentDetails)
    {
        lock.lock();
        String result;
        if(!IdValidator.isValidAdminAppointment(appointmentDetails))
        {
            result = "FAIL";
        } else if(containsAppointmentID(appointmentDetails))
            result = "FAIL";
        else {
            database.get(appointmentDetails.getAppointmentType()).put(appointmentDetails.getAppointmentID(), appointmentDetails);
            result = "SUCCESS";
        }
        lock.unlock();
        return result;
    }

    public String cancelAndHold(String patientID, String appointmentID, AppointmentType appointmentType)
    {
        lock.lock();
        String result;
        boolean removed;
        if(!IdValidator.isPatientID(patientID))
            result = "FAIL";
        else if(!containsAppointmentID(appointmentType, appointmentID)) {
            result = "FAIL";
        } else {
                removed = getAppointmentsForType(appointmentType).get(appointmentID).removePatientAndHold(patientID);
            result =  removed ? "SUCCESS" : "FAIL";
        }
        lock.unlock();
        return result;
    }

    public String unholdAndRebook(String patientID, String appointmentID, AppointmentType appointmentType)
    {
        lock.lock();

        String result;
        if(!IdValidator.isPatientID(patientID))
            result =  "FAIL";
        else if(!containsAppointmentID(appointmentType, appointmentID))
            result = "FAIL";
        else if(hasExistingAppointmentTypeThatDay(appointmentType, appointmentID, patientID))
            result = "FAIL";
        else {
            getAppointmentsForType(appointmentType).get(appointmentID).unhold();
            result = getAppointmentsForType(appointmentType).get(appointmentID).addPatient(patientID);
        }
        lock.unlock();
        return result;
    }

    public String unHold(String appointmentID, AppointmentType appointmentType)
    {
        lock.lock();
        getAppointmentsForType(appointmentType).get(appointmentID).unhold();
        lock.unlock();
        return "SUCCESS";
    }

    public String addAppointment(String appointmentId, AppointmentType appointmentType, int capacity)
    {
        return addAppointment(new AppointmentDetails(capacity, appointmentId,appointmentType));
    }

    public String removeAppointment(AppointmentDetails appointmentDetails)
    {
        return removeAppointment(appointmentDetails.getAppointmentID(), appointmentDetails.getAppointmentType());
    }

    public String removeAppointment(String appointmentID, AppointmentType appointmentType)
    {
        lock.lock();
        String result;
        if(!containsAppointmentID(appointmentType, appointmentID))
            result = "FAIL";
        else {
            Set<String> bookedPatients = database.get(appointmentType).get(appointmentID).getPatientsBooked();
            bookedPatients.forEach(patient -> {
                AppointmentDetails nextEntry = getNextEntry(database.get(appointmentType), appointmentID, appointmentType);
                if (Objects.nonNull(nextEntry))
                    nextEntry.addPatient(patient);
            });
            database.get(appointmentType).remove(appointmentID);
            result = "SUCCESS";
        }
        lock.unlock();
        return result;
    }

    public String listAppointment(AppointmentType appointmentType)
    {
        lock.lock();
        String result = database.get(appointmentType).values().stream()
                .map(item -> ";" + item.getAppointmentID() + " " + item.getCapacity())
                .sorted()
                .reduce("", (item1, item2) -> item1+item2);
        lock.unlock();
        return result;
    }

    public String bookAppointment(AppointmentType appointmentType, String appointmentID, String patientID)
    {
        lock.lock();
        String result;
        if(!IdValidator.isPatientID(patientID))
            result =  "FAIL";
        else if(!containsAppointmentID(appointmentType, appointmentID))
            result = "FAIL";
        else if(hasExistingAppointmentTypeThatDay(appointmentType, appointmentID, patientID))
            result = "FAIL";
        else {
            result = getAppointmentsForType(appointmentType).get(appointmentID).addPatient(patientID);
        }
        lock.unlock();
        return  result;
    }
    
    public String getAppointmentSchedule(String patientID)
    {
        lock.lock();
        String result;
        if(!IdValidator.isPatientID(patientID))
            result = "FAIL";
        else {
            StringBuilder stringBuilder = new StringBuilder();
            database.values().forEach(typeMap ->
                    typeMap.values().stream()
                            .filter(appointmentDetails -> appointmentDetails.getPatientsBooked().contains(patientID))
                            .forEach(appointmentDetails -> stringBuilder.append(
                                    ";" + StringConversion.getAppointmentTypeString(appointmentDetails.getAppointmentType()) +
                                            " " + appointmentDetails.getAppointmentID()))
            );
            result = stringBuilder.toString();
        }
        lock.unlock();
        return result;
    }

    public String cancelAppointment(String patientID, String appointmentID)
    {
        lock.lock();
        String result;
        boolean removed = false;
        if(!IdValidator.isPatientID(patientID))
            result = "FAIL";
        else if(!containsAppointmentID(AppointmentType.Dental, appointmentID) &&
                !containsAppointmentID(AppointmentType.Surgeon, appointmentID) &&
                !containsAppointmentID(AppointmentType.Physician, appointmentID)) {
            result = "FAIL";
        } else {
            if (containsAppointmentID(AppointmentType.Dental, appointmentID))
                removed = getAppointmentsForType(AppointmentType.Dental).get(appointmentID).removePatient(patientID);
            if (containsAppointmentID(AppointmentType.Surgeon, appointmentID))
                removed = removed || getAppointmentsForType(AppointmentType.Surgeon).get(appointmentID).removePatient(patientID);
            if (containsAppointmentID(AppointmentType.Physician, appointmentID))
                removed = removed || getAppointmentsForType(AppointmentType.Physician).get(appointmentID).removePatient(patientID);
            result =  removed ? "SUCCESS" : "FAIL";
        }
        lock.unlock();
        return result;
    }

    public HashMap<String, AppointmentDetails> getAppointmentsForType(AppointmentType appointmentType)
    {
        lock.lock();
        HashMap<String, AppointmentDetails> appointmentDetailsHashMap =  database.get(appointmentType);
        lock.unlock();
        return appointmentDetailsHashMap;
    }

    private boolean containsAppointmentID(AppointmentDetails appointmentDetails)
    {
        return containsAppointmentID(appointmentDetails.getAppointmentType(), appointmentDetails.getAppointmentID());
    }

    public long numberOfAppointmentsInWeek(String appointmentID, String patientID)
    {
        lock.lock();
        long result;
        long week = AppointmentDetails.getAppointmentWeek(appointmentID);
        result = database.values().stream()
                .mapToLong(appointmentHash -> appointmentHash.values().stream()
                        .filter(appointmentDetails -> AppointmentDetails.getAppointmentWeek(appointmentDetails.getAppointmentID()) == week)
                        .filter(appointmentDetails -> appointmentDetails.containsPatient(patientID))
                        .count())
                .reduce(0, (item1, item2) -> item1 + item2);
        lock.unlock();
        return result;
    }

    private boolean containsAppointmentID(AppointmentType appointmentType, String appointmentID)
    {
        return database.get(appointmentType).containsKey(appointmentID);
    }

    private boolean hasExistingAppointmentTypeThatDay(
            AppointmentType appointmentType, String appointmentID, String patientID)
    {
        HashMap<String, AppointmentDetails> appointmentsOfType = getAppointmentsForType(appointmentType);
        LocalDate appointmentDate = AppointmentDetails.getAppointmentDate(appointmentID);

        ArrayList<AppointmentDetails> allAppointmentsThatDay = (ArrayList<AppointmentDetails>)appointmentsOfType.keySet().stream()
                .filter(appointment -> appointmentDate.equals(AppointmentDetails.getAppointmentDate(appointment)))
                .map(appointmentsOfType::get) //all AppointmentDetails with correct date
                .collect(Collectors.toList());

        return allAppointmentsThatDay.stream().anyMatch(item -> item.getPatientsBooked().contains(patientID));
    }

    private AppointmentDetails getNextEntry(HashMap<String, AppointmentDetails> appointmentList, String appointmentID, AppointmentType appointmentType)
    {
        AppointmentDetails appointmentDetails = new AppointmentDetails(0,appointmentID, appointmentType);
        return appointmentList.values().stream()
                .filter(item -> item.getCapacity() > 0)
                .filter(item -> item.compareTo(appointmentDetails) > 0)
                .min(AppointmentDetails::compareTo).orElse(null);
    }

    public static void resetDatabase()
    {
        ONLY_INSTANCE = new Database();
    }
}
