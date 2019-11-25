package corbasystem;


/**
* corbasystem/_IFrontEndServerStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from corbasystem.idl
* Sunday, November 24, 2019 9:41:38 o'clock PM EST
*/

public class _IFrontEndServerStub extends org.omg.CORBA.portable.ObjectImpl implements corbasystem.IFrontEndServer
{

  public String initiateServer (String city)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("initiateServer", true);
                $out.write_string (city);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return initiateServer (city        );
            } finally {
                _releaseReply ($in);
            }
  } // initiateServer

  public String requestHandler (String userId, String command, String parameters)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("requestHandler", true);
                $out.write_string (userId);
                $out.write_string (command);
                $out.write_string (parameters);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return requestHandler (userId, command, parameters        );
            } finally {
                _releaseReply ($in);
            }
  } // requestHandler

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:corbasystem/IFrontEndServer:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     org.omg.CORBA.Object obj = orb.string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
   } finally {
     orb.destroy() ;
   }
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     String str = orb.object_to_string (this);
     s.writeUTF (str);
   } finally {
     orb.destroy() ;
   }
  }
} // class _IFrontEndServerStub
