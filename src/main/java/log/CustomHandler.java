package log;

import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class CustomHandler extends StreamHandler {

    @Override
    public synchronized void publish(LogRecord logRecord) {
        // add own logic to publish
        super.publish(logRecord);
    }

    @Override
    public synchronized void flush() {
        super.flush();
    }

    @Override
    public synchronized void close() throws SecurityException {
        super.close();
    }
}
