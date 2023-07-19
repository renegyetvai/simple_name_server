package nameserver;

import communication.format.Message;
import service.IService;

import java.net.DatagramPacket;

public class NameService implements IService {
    private final static int UDP_BUFFER_SIZE = 512;
    private final NameData nameData;

    public NameService(String rootName) {
        nameData = new NameData(rootName);
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
                return new Message(Message.messageTypes.MSG_REGISTER_REPLY, registerAnswer.toString());
            }
            case MSG_RESOLVE_REQUEST -> {
                Node resolveAnswer = resolveName(payload);
                assert resolveAnswer != null;
                String serviceAttributes = resolveAnswer.getName() + " " + resolveAnswer.getIp() + " " + resolveAnswer.getPort();
                return new Message(Message.messageTypes.MSG_RESOLVE_REPLY, serviceAttributes);
            }
            case MSG_DELETE_REQUEST -> {
                boolean deleteAnswer = deleteName(payload);
                if (deleteAnswer) {
                    System.err.println("Successfully deleted " + payload);
                    return new Message(Message.messageTypes.MSG_DELETE_REPLY, "Successfully deleted " + payload);
                } else {
                    System.err.println("Failed to delete " + payload);
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
