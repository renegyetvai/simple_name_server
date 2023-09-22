package log.config;

import log.CustomFilter;
import log.CustomFormatter;
import log.CustomHandler;

import java.io.*;
import java.util.logging.*;

public class LoggerConfig {
    public static void configureLogger(Logger logger) {
        configureLoggingProperties();
        configureHandlers(logger);
        configureLogLevel(logger);
    }

    private static void configureLoggingProperties() {
        String relativePath = "log/config/customLogging.properties";
        ClassLoader classLoader = LoggerConfig.class.getClassLoader();

        try (InputStream stream = classLoader.getResourceAsStream(relativePath)) {
            if (stream == null) {
                throw new FileNotFoundException("Could not find resource: " + relativePath);
            }

            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            System.err.println("Error while loading resource: " + relativePath);
        }
    }

    private static void configureHandlers(Logger logger) {
        logger.addHandler(new ConsoleHandler());
        logger.addHandler(new CustomHandler());

        try {
            String timeStamp = new java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new java.util.Date());
            File dumpFolder = new File("dump");

            if (!dumpFolder.exists() && dumpFolder.mkdir()) {
                System.err.println("Created dump folder");
            }

            Handler fileHandler = new FileHandler(dumpFolder + "/CustomLogger_" + timeStamp + ".log", 2000, 5);
            fileHandler.setFormatter(new CustomFormatter());
            fileHandler.setFilter(new CustomFilter());
            logger.addHandler(fileHandler);
        } catch (SecurityException | IOException e) {
            System.err.println("Failed to create logging file: \n" + e.getMessage());
        }
    }

    private static void configureLogLevel(Logger logger) {
        logger.setLevel(Level.FINE);
    }
}
