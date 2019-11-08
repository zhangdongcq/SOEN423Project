package logging;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    public static synchronized void saveLog(String request, String response, String userID) throws IOException
    {
        String pathString = "src/main/resources/" + userID + ".txt";
        File logFile = new File(pathString);
        PrintWriter output;
        if(logFile.exists())
        {
            output = new PrintWriter(new FileOutputStream(new File(pathString), true));
        } else {
            output = new PrintWriter(pathString);
        }
        String logString = getLogDateTime() + "Request:[ " + request + " ] Response:[ " + response + " ]";
        output.println(logString);
        output.close();

    }

    private static String getLogDateTime()
    {
        LocalDateTime localDateTime = LocalDateTime.now();
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append(localDateTime.toLocalDate());
        stringBuilder.append(" ");
        stringBuilder.append(localDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ssa")));
        stringBuilder.append(" > " );
        return stringBuilder.toString();
    }


}