package corbasystem;


/**
* corbasystem/IFrontEndServerOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from corbasystem.idl
* Sunday, November 24, 2019 10:52:32 o'clock PM EST
*/

public interface IFrontEndServerOperations 
{
  String initiateServer (String city);
  String requestHandler (String userId, String command, String parameters);
} // interface IFrontEndServerOperations
