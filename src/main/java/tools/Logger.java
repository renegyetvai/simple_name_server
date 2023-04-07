package tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    /**
     * The log levels of the logger.
     */
    public enum LogLevel {
        INFO,
        DEBUG
    }

    private FileOutputStream file;
    private Boolean useConsole;
    private final LogLevel level;

    public Logger(LogLevel level, Path path) {
        this.level = level;
        if (!path.toString().equals("")) {
            useConsole = false;
            try {
                file = new FileOutputStream(path.toString());
            } catch (FileNotFoundException e) {
                useConsole = true;
                System.err.println("Path of Logfile could not be found! Using Console instead!");
            }
        }
        else {
            useConsole = true;
        }
    }

    /**
     * Logs a message.
     * @param msg The message to log.
     */
    public void log(String msg) {
        assert msg != null;
        String time = getCurrentTime();

        if (useConsole) {
            if (level.equals(LogLevel.DEBUG)) {
                System.err.println(time + ": Message: " + msg);
            } else {
                System.out.println(time + ": Message: " + msg);
            }
        } else {
            String message = time + ": Message: " + msg;
            byte[] strToBytes = message.getBytes();
            try {
                file.write(strToBytes);
            } catch (IOException e) {
                System.err.println("Can not write to file! " + e.getMessage()); // Maybe e.getMessage() not needed!
                System.err.println("Original log message: ");
                System.err.println(time + ": Message: " + msg);
            }
        }
    }

    /**
     * Creates a string containing the current time.
     * @return Current time timestamp.
     */
    private String getCurrentTime() {
        SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss");
        return date.format(new Date());
    }
}
