package interaction;


import RemotelyInvokableHospitalApp.RemotelyInvokableHospital;

import java.util.List;

public class CorbaTester extends Thread {

    private List<String> arguments;
    private RemotelyInvokableHospital remotelyInvokableHospital;

    public CorbaTester(List<String> arguments, RemotelyInvokableHospital remotelyInvokableHospital)
    {
        this.arguments = arguments;
        this.remotelyInvokableHospital = remotelyInvokableHospital;
    }


    @Override
    public void run() {
        deliverArguments(arguments, remotelyInvokableHospital);
    }

    public static String deliverArguments(List<String> arguments, RemotelyInvokableHospital remotelyInvokableHospital)
    {
        String result = CorbaDelivery.deliverCorbaRequest(arguments, remotelyInvokableHospital);
        System.out.println(result);
        System.out.flush();
        return result;
    }
}
