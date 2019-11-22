package corbasystem;


/**
* corbasystem/IFrontEndServerHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from corbasystem.idl
* Friday, November 1, 2019 8:44:19 o'clock AM EDT
*/

abstract public class IFrontEndServerHelper
{
  private static String  _id = "IDL:corbasystem/IFrontEndServer:1.0";

  public static void insert (org.omg.CORBA.Any a, corbasystem.IFrontEndServer that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static corbasystem.IFrontEndServer extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (corbasystem.IFrontEndServerHelper.id (), "IFrontEndServer");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static corbasystem.IFrontEndServer read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_IFrontEndServerStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, corbasystem.IFrontEndServer value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static corbasystem.IFrontEndServer narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof corbasystem.IFrontEndServer)
      return (corbasystem.IFrontEndServer)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      corbasystem._IFrontEndServerStub stub = new corbasystem._IFrontEndServerStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static corbasystem.IFrontEndServer unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof corbasystem.IFrontEndServer)
      return (corbasystem.IFrontEndServer)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      corbasystem._IFrontEndServerStub stub = new corbasystem._IFrontEndServerStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
