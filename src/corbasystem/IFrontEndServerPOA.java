package corbasystem;


/**
* corbasystem/IFrontEndServerPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from corbasystem.idl
* Sunday, November 24, 2019 10:52:32 o'clock PM EST
*/

public abstract class IFrontEndServerPOA extends org.omg.PortableServer.Servant
 implements corbasystem.IFrontEndServerOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("initiateServer", new java.lang.Integer (0));
    _methods.put ("requestHandler", new java.lang.Integer (1));
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
       case 0:  // corbasystem/IFrontEndServer/initiateServer
       {
         String city = in.read_string ();
         String $result = null;
         $result = this.initiateServer (city);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 1:  // corbasystem/IFrontEndServer/requestHandler
       {
         String userId = in.read_string ();
         String command = in.read_string ();
         String parameters = in.read_string ();
         String $result = null;
         $result = this.requestHandler (userId, command, parameters);
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
    "IDL:corbasystem/IFrontEndServer:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public IFrontEndServer _this() 
  {
    return IFrontEndServerHelper.narrow(
    super._this_object());
  }

  public IFrontEndServer _this(org.omg.CORBA.ORB orb) 
  {
    return IFrontEndServerHelper.narrow(
    super._this_object(orb));
  }


} // class IFrontEndServerPOA
