package nameserver;

import communication.format.Message;
import log.CustomFilter;
import log.CustomFormatter;
import log.CustomHandler;
import service.IService;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.logging.*;

public class NameService implements IService {
    public static Logger logger = Logger.getLogger(Node.class.getName());
    private final static int UDP_BUFFER_SIZE = 512;
    private final NameData nameData;

    public NameService(String rootName) {
        nameData = new NameData(rootName);

        // --------- LOGGER ---------
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("src/main/resources/log/config/customLogging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }

        logger.addHandler(new ConsoleHandler());
        // Adding custom handler
        logger.addHandler(new CustomHandler());

        logger.setLevel(Level.FINE);

        try {
            // FileHandler file name with max size and number of log files limit
            String timeStamp = new java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new java.util.Date());
            Handler fileHandler = new FileHandler("src/main/resources/log/dump/CustomLogger_" + timeStamp + ".log", 2000, 5);
            fileHandler.setFormatter(new CustomFormatter());
            // Setting custom filter for FileHandler
            fileHandler.setFilter(new CustomFilter());
            logger.addHandler(fileHandler);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        // --------- LOGGER END ---------
    }

    @Override
    public Message process(DatagramPacket packet) {
        // Convert from a DatagramPacket to a Message object
        Message message = convertPacketToMessage(packet);

        // Save message type and payload to variables.
        Message.messageTypes type = message.getMessageTypeEnum(message.getMessageType());
        String payload = message.getPayload();

        return handleMessage(type, payload);
    }

    private synchronized Message convertPacketToMessage(DatagramPacket packet) {
        byte[] data = packet.getData();
        return Message.readFromBytes(data);
    }

    private synchronized Message handleMessage(Message.messageTypes type, String payload) {
        // Handle the message.
        switch (type) {
            case MSG_REGISTER_REQUEST -> {
                StringBuilder registerAnswer = new StringBuilder(registerName(payload.split(" ")[0], payload.split(" ")[1], Integer.parseInt(payload.split(" ")[2])));

                // Fill register answer with "\0" until 512 bytes since this is the maximum UDP payload size.
                while (registerAnswer.length() < UDP_BUFFER_SIZE) {
                    registerAnswer.append("\0");
                }
                logger.log(Level.INFO, "Registering " + payload.split(" ")[0] + " with IP " + payload.split(" ")[1] + " and port " + payload.split(" ")[2]);
                return new Message(Message.messageTypes.MSG_REGISTER_REPLY, registerAnswer.toString());
            }
            case MSG_RESOLVE_REQUEST -> {
                Node resolveAnswer = resolveName(payload);
                assert resolveAnswer != null;
                String serviceAttributes = resolveAnswer.getName() + " " + resolveAnswer.getIp() + " " + resolveAnswer.getPort();
                logger.log(Level.INFO, "Resolving " + payload + " to " + serviceAttributes);
                return new Message(Message.messageTypes.MSG_RESOLVE_REPLY, serviceAttributes);
            }
            case MSG_DELETE_REQUEST -> {
                boolean deleteAnswer = deleteName(payload);
                if (deleteAnswer) {
                    logger.log(Level.INFO, "Deleting " + payload);
                    return new Message(Message.messageTypes.MSG_DELETE_REPLY, "Successfully deleted " + payload);
                } else {
                    logger.log(Level.INFO, "Failed to delete " + payload);
                    return new Message(Message.messageTypes.MSG_DELETE_ERROR, "Failed to delete " + payload);
                }
            }
        }
        return null;
    }

    private String registerName(String name, String ip, int port) {
        return nameData.addNode(name, ip, port);
    }

    private Node resolveName(String name) {
        return nameData.getNode(name);
    }

    private Boolean deleteName(String name) {
        return nameData.removeNode(name, NameData.removeType.NORMAL);
    }
}
