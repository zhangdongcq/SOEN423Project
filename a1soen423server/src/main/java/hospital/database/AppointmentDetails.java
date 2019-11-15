package hospital.database;

import RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType.Dental;

public class AppointmentDetails implements Comparable<AppointmentDetails> {

    private int capacity;

    @Override
    public int compareTo(AppointmentDetails other) {
        boolean datesAreEqual = this.getAppointmentDate().equals(other.getAppointmentDate());
        boolean daysAreEqual = this.getAppointmentDaySlotNumber() == other.getAppointmentDaySlotNumber();
        boolean otherDayIsSmaller = this.getAppointmentDaySlotNumber() > other.getAppointmentDaySlotNumber();
        boolean otherDayIsLarger = this.getAppointmentDaySlotNumber() < other.getAppointmentDaySlotNumber();

        if(datesAreEqual && daysAreEqual)
            return 0;
        if((datesAreEqual && otherDayIsSmaller))
            return 1;
        if((datesAreEqual && otherDayIsLarger))
            return -1;
        return this.getAppointmentDate().compareTo(other.getAppointmentDate());
    }

    private Set<String> patientsBooked;
    private String appointmentID;
    private AppointmentType appointmentType;

    public AppointmentDetails(int capacity, String appointmentID, AppointmentType appointmentType)
    {
        this.appointmentType = appointmentType;

        this.appointmentID = appointmentID;
        this.capacity = capacity;
        patientsBooked = new HashSet<>();
    }

    public boolean containsPatient(String patientID)
    {
        return patientsBooked.contains(patientID);
    }

    public String addPatient(String patientID)
    {
        if(capacity > 0)
        {
            patientsBooked.add(patientID);
            capacity--;
            return "SUCCESS";
        }
        return "FAIL";
    }

    public boolean removePatientAndHold(String patientId)
    {
        boolean removePatient = removePatient(patientId);
        if(removePatient)
        {
            capacity--;
        }
        return removePatient;
    }

    public void unhold()
    {
        capacity++;
    }

    public boolean removePatient(String patientID)
    {
        if(containsPatient(patientID))
        {
            patientsBooked.remove(patientID);
            capacity++;
            return true;
        }
        return false;
    }

    public int getCapacity() {
        return capacity;
    }

    public Set<String> getPatientsBooked() {
        return patientsBooked;
    }

    public String getAppointmentID() {
        return appointmentID;
    }

    public static LocalDate getAppointmentDate(String appointmentID)
    {
        final int dateSubstring = 4;
        DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("ddMMyy");
        return LocalDate.parse(appointmentID.substring(dateSubstring), datePattern);
    }

    public LocalDate getAppointmentDate()
    {
        return getAppointmentDate(getAppointmentID());
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    /**
     *
     * @param appointmentID
     * @return1 1 for M, 2 for A, 3 for E
     */
    public static int getAppointmentDaySlotNumber(String appointmentID)
    {
        String daySlot = appointmentID.substring(3,4);
        switch (daySlot) {
            case "M":
                return 1;
            case "A":
                return 2;
            case "E":
                return 3;
            default:
                return -1;
        }
    }

    /**
     *
     * @param appointmentID The appointment needing week resolution
     * @return Number of months passed since 1Jan 2000 in week form, plus one week for days (1-7, 8-14, 15-21, 22-28)
     */
    public static long getAppointmentWeek(String appointmentID)
    {
        LocalDate givenDate = getAppointmentDate(appointmentID);
        LocalDate start = LocalDate.of(2000, 1,1);
        long months = ChronoUnit.MONTHS.between(start.withDayOfMonth(1), givenDate.withDayOfMonth(1));
        long days = givenDate.getDayOfMonth();
        long week;
        if(days%7 == 0)
            week =  days/7;
        else
            week = 1 + days/7; //this should give an int between 0 and 4
        if(week == 5) // we want week 22-28 to be the same as days 28-31
            week = 4;
        return week + (months*4);
    }

    public static String getAppointmentHospital(String appointmentID)
    {
        return appointmentID.substring(0,3);
    }

    /**
     *
     * @return 1 for M, 2 for A, 3 for E
     */
    public int getAppointmentDaySlotNumber()
    {
        return getAppointmentDaySlotNumber(getAppointmentID());
    }

}
