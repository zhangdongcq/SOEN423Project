import java.io.BufferedWriter;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	
	private BufferedWriter out;
    SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
    //constructor
	public Log (String fileName){
        try {
            out = new BufferedWriter(new FileWriter(fileName, true));
        }catch (Exception e){
            System.out.println("Logfile open failed.");
        }
    }

   
    protected void finalize(){
        try{
            out.close();
        }catch (Exception e){
            System.out.println("Logfile close failed.");
        }
    }

    
    public synchronized String  writeLog(String msg){
        try{
            out.write(date.format(new Date())+": "+msg+"\r\n");
            out.flush();
        }catch (Exception e){
            System.out.println("Write to logfile failed.");
        }
        return(msg);
    }
	
}