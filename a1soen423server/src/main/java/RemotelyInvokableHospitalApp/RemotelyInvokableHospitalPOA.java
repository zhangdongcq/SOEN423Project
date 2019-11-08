package RemotelyInvokableHospitalApp;


/**
* RemotelyInvokableHospitalApp/RemotelyInvokableHospitalPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from RemotelyInvokableHospitalCorba.idl
* Tuesday, October 8, 2019 6:04:55 o'clock PM EDT
*/

public abstract class RemotelyInvokableHospitalPOA extends org.omg.PortableServer.Servant
 implements RemotelyInvokableHospitalApp.RemotelyInvokableHospitalOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("addAppointment", new java.lang.Integer (0));
    _methods.put ("removeAppointment", new java.lang.Integer (1));
    _methods.put ("listAppointmentAvailability", new java.lang.Integer (2));
    _methods.put ("bookAppointment", new java.lang.Integer (3));
    _methods.put ("getAppointmentSchedule", new java.lang.Integer (4));
    _methods.put ("cancelAppointment", new java.lang.Integer (5));
    _methods.put ("swapAppointment", new java.lang.Integer (6));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // RemotelyInvokableHospitalApp/RemotelyInvokableHospital/addAppointment
       {
         String appointmentId = in.read_string ();
         RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType appointmentType = RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentTypeHelper.read (in);
         int capacity = in.read_long ();
         String $result = null;
         $result = this.addAppointment (appointmentId, appointmentType, capacity);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 1:  // RemotelyInvokableHospitalApp/RemotelyInvokableHospital/removeAppointment
       {
         String appointmentId = in.read_string ();
         RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType appointmentType = RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentTypeHelper.read (in);
         String $result = null;
         $result = this.removeAppointment (appointmentId, appointmentType);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 2:  // RemotelyInvokableHospitalApp/RemotelyInvokableHospital/listAppointmentAvailability
       {
         RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType appointmentType = RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentTypeHelper.read (in);
         String $result = null;
         $result = this.listAppointmentAvailability (appointmentType);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // RemotelyInvokableHospitalApp/RemotelyInvokableHospital/bookAppointment
       {
         String patientId = in.read_string ();
         String appointmentId = in.read_string ();
         RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType appointmentType = RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentTypeHelper.read (in);
         String $result = null;
         $result = this.bookAppointment (patientId, appointmentId, appointmentType);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 4:  // RemotelyInvokableHospitalApp/RemotelyInvokableHospital/getAppointmentSchedule
       {
         String patientId = in.read_string ();
         String $result = null;
         $result = this.getAppointmentSchedule (patientId);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // RemotelyInvokableHospitalApp/RemotelyInvokableHospital/cancelAppointment
       {
         String patientId = in.read_string ();
         String appointmentId = in.read_string ();
         String $result = null;
         $result = this.cancelAppointment (patientId, appointmentId);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 6:  // RemotelyInvokableHospitalApp/RemotelyInvokableHospital/swapAppointment
       {
         String patientId = in.read_string ();
         String oldAppointmentId = in.read_string ();
         RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType oldAppointmentType = RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentTypeHelper.read (in);
         String newAppointmentId = in.read_string ();
         RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentType newAppointmentType = RemotelyInvokableHospitalApp.RemotelyInvokableHospitalPackage.AppointmentTypeHelper.read (in);
         String $result = null;
         $result = this.swapAppointment (patientId, oldAppointmentId, oldAppointmentType, newAppointmentId, newAppointmentType);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:RemotelyInvokableHospitalApp/RemotelyInvokableHospital:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public RemotelyInvokableHospital _this() 
  {
    return RemotelyInvokableHospitalHelper.narrow(
    super._this_object());
  }

  public RemotelyInvokableHospital _this(org.omg.CORBA.ORB orb) 
  {
    return RemotelyInvokableHospitalHelper.narrow(
    super._this_object(orb));
  }


} // class RemotelyInvokableHospitalPOA
