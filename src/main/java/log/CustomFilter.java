package log;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class CustomFilter implements Filter {

	@Override
	public boolean isLoggable(LogRecord log) {
		//don't log CONFIG logs in file
		return log.getLevel() != Level.CONFIG;
	}

}
