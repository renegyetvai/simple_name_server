package nameserver;

import communication.format.Message;
import communication.format.Message.messageTypes;
import communication.service.INetworkService;
import communication.service.Service;
import communication.udp.server.UDPServer;
import tools.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NameServer implements INameService, INetworkService {

    private final Path path = Paths.get("");
    private final Logger logger = new Logger(Logger.LogLevel.DEBUG, path);
    private String address = "";
    private int port = 5555;
    private final NameData nameData;
    private UDPServer udpServer;

    public NameServer(String rootName) {
        nameData = new NameData(rootName);
    }

    @Override
    public Service resolveName(String name) {
        Node node = nameData.getNode(name);
        if (node == null) {
            return null;
        } else {
            System.out.println(node.getName() + " " + node.getIp() + " " + node.getPort());
            System.out.println(node.getFullName());

            return new Service(node.getFullName(), node.getIp(), node.getPort());
        }
    }

    @Override
    public String registerName(String name, String ip, int port) {
        return this.nameData.addNode(name, ip, port);
    }

    @Override
    public Boolean registerName(Service service) {
        String result = this.nameData.addNode(service.getName(), service.getIP(), service.getPort());
        return result != null;
    }

    @Override
    public synchronized void send(InetAddress address, int port, Message msg) throws IOException {
        DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, port);
        udpServer.getServerSocket().send(packet);
    }

    @Override
    public synchronized void receive(DatagramPacket packet) throws IOException {

        // Get data from packet
        byte[] data = packet.getData();

        // Convert the received message from a byte array to a message object.
        Message message = Message.readFromBytes(data);

        // Save message type and payload to variables.
        messageTypes type = message.getMessageTypeEnum(message.getMessageType());
        String payload = message.getPayload();

        logger.log("Received message at Nameservice: " + type + " " + payload);

        // Handle the message.
        switch (type) {
            case MSG_REGISTER -> {
                // TODO: Handle register with service as payload.
                StringBuilder registerAnswer = new StringBuilder(registerName(payload.split(" ")[0], payload.split(" ")[1], Integer.parseInt(payload.split(" ")[2])));

                // Fill register answer with "\0" until 512 bytes since this is the maximum UDP payload size.
                while (registerAnswer.length() < 512) {
                    registerAnswer.append("\0");
                }
                Message msgRegisterReply = new Message(messageTypes.MSG_REGISTER_REPLY, registerAnswer.toString());
                send(packet.getAddress(), packet.getPort(), msgRegisterReply);
            }
            case MSG_RESOLVE_REQUEST -> {
                Message msgResolveReply;
                Service resolveAnswer = resolveName(payload);
                String serviceAttributes = resolveAnswer.getName() + " " + resolveAnswer.getIP() + " " + resolveAnswer.getPort();
                msgResolveReply = new Message(messageTypes.MSG_RESOLVE_REPLY, serviceAttributes);
                send(packet.getAddress(), packet.getPort(), msgResolveReply);
            }
        }
    }

    @Override
    public String getName() {
        return "NameService";
    }

    @Override
    public int getPort() {
        return port;
    }
    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getIP() {
        return address;
    }

    @Override
    public void setName(String name) {
        System.err.println("NameServer.setName() is not supported.");
    }

    @Override
    public void setIP(String ip) {
        this.address = ip;
    }

    public void setUdpServer(UDPServer udpServer) {
        this.udpServer = udpServer;
    }

    public static Class<?> getClassByName(String service) {
        Class<?> clazz = null;
        try {
            // TODO: Check if this is the correct way to do this.
            clazz = Class.forName(service.split("/")[0]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }
}
