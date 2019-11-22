package DhmsApp;


/**
* DhmsApp/DhmsOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Dhms.idl
* Friday, November 22, 2019 1:09:15 o'clock PM EST
*/

public interface DhmsOperations 
{
  String addAppointment (String appointmentID, String appointmentType, int capacity);
  String removeAppointment (String appointmentID, String appointmentType);
  String listAppointmentAvailability (String appointmentType);
  String bookAppointment (String patientID, String appointmentID, String appointmentType);
  String getAppointmentSchedule (String patientID);
  String cancelAppointment (String patientID, String appointmentID);
  String swapAppointment (String patientID, String oldAppointmentID, String oldAppointmentType, String newAppointmentID, String newAppointmentType);
  int loginAdmin (String adminID);
  int logoutAdmin (String adminID);
  int loginPatient (String patientID);
  int logoutPatient (String patientID);
  boolean validateAppointmentID (String id);
  boolean validateAppointmentType (String type);
  String showAppointmentRecords ();
  String showPatientRecords ();
} // interface DhmsOperations
