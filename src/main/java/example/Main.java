package example;

import communication.format.Message;
import communication.udp.client.UDPClient;
import communication.udp.server.UDPServer;
import log.CustomFilter;
import log.CustomFormatter;
import log.CustomHandler;
import nameserver.NameService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.logging.*;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws InterruptedException, IOException {
        // --------- LOGGER ---------
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("src/main/resources/log/config/customLogging.properties"));
        } catch (SecurityException | IOException e) {
            System.err.println("Failed to read logging configuration file: \n" + e.getMessage());
        }

        logger.addHandler(new ConsoleHandler());
        // Adding custom handler
        logger.addHandler(new CustomHandler());

        logger.setLevel(Level.FINE);

        try {
            String timeStamp = new java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new java.util.Date());
            // Create dump folder if it doesn't exist
            File dumpFolder = new File("src/main/resources/log/dump");
            if (!dumpFolder.exists()) {
                if (dumpFolder.mkdir()) {
                    System.err.println("Created dump folder");
                } else {
                    System.err.println("Failed to create dump folder");
                }
            }
            // FileHandler file name with max size and number of log files limit
            Handler fileHandler = new FileHandler("src/main/resources/log/dump/CustomLogger_" + timeStamp + ".log", 2000, 5);
            fileHandler.setFormatter(new CustomFormatter());
            // Setting custom filter for FileHandler
            fileHandler.setFilter(new CustomFilter());
            logger.addHandler(fileHandler);
        } catch (SecurityException | IOException e) {
            System.err.println("Failed to create logging file: \n" + e.getMessage());
        }
        // --------- LOGGER END ---------

        if (args.length < 1) {
            logger.log(Level.SEVERE, "Missing arguments");
            System.exit(0);
        }

        switch (args[0]) {
            case "-s" -> {
                NameService serviceProvider = new NameService("local");
                Thread serverThread = new Thread(new UDPServer(5555, serviceProvider));
                logger.log(Level.INFO, "Starting server");
                serverThread.start();
                serverThread.join();
            }
            case "-c" -> {
                DatagramPacket answerOne;
                DatagramPacket answerTwo;
                DatagramPacket answerThree;
                DatagramPacket answerFour;
                Message[] messages = new Message[4];

                logger.log(Level.INFO, "Starting client");
                UDPClient udpClient = new UDPClient("localhost", 5555);

                logger.log(Level.INFO, "Sending messages");
                answerOne = udpClient.sendPacket(new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "home\\local 1.2.3.4 8091"));
                answerTwo = udpClient.sendPacket(new Message(Message.messageTypes.MSG_RESOLVE_REQUEST, "home\\local"));
                answerThree = udpClient.sendPacket(new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "rg\\home\\local 1.2.3.5 8092"));
                answerFour = udpClient.sendPacket(new Message(Message.messageTypes.MSG_RESOLVE_REQUEST, "rg\\home\\local"));

                udpClient.close();

                // Convert the received message from a byte array to a message object.
                messages[0] = Message.readFromBytes(answerOne.getData());
                messages[1] = Message.readFromBytes(answerTwo.getData());
                messages[2] = Message.readFromBytes(answerThree.getData());
                messages[3] = Message.readFromBytes(answerFour.getData());

                for (Message message : messages) {
                    if (message == null) {
                        logger.log(Level.SEVERE, "Message is null");
                        continue;
                    }
                    logger.log(Level.INFO, "Received answer: " + "type = " + message.getMessageType() + " & content = '" + message.getPayload() + "'");
                }
                System.out.println("Client finished");
            }
            default -> System.out.println("Provided parameter was not found!\n " +
                    "Please enter parameter '-s' to start as server or '-c' to start as client!");
        }
    }
}
