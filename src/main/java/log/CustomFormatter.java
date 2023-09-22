package log;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter {

    @Override
    public String format(LogRecord logRecord) {
        return logRecord.getLongThreadID() + "::" + logRecord.getSourceClassName() + "::"
                + logRecord.getSourceMethodName() + "::"
                + new Date(logRecord.getMillis()) + "::"
                + logRecord.getMessage() + "\n";
    }

}
