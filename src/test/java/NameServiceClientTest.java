import communication.format.Message;
import communication.udp.client.UDPClient;
import communication.udp.server.UDPServer;
import nameserver.NameService;
import org.junit.jupiter.api.Test;
import service.IService;

import java.io.IOException;
import java.net.DatagramPacket;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameServiceClientTest {

    private Thread setupServer(int port) throws InterruptedException {
        IService nameService = new NameService("root");
        Thread serverThread = new Thread(new UDPServer(port, nameService));
        serverThread.start();
        serverThread.join();

        return serverThread;
    }

    @Test
    void clientResolveTest() throws IOException, InterruptedException {
        Thread serverThread = setupServer(5555);

        DatagramPacket answer;
        Message message;

        UDPClient UDPClient = new UDPClient("localhost", 5555);

        UDPClient.sendPacket(new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "root.home 127.0.0.1 80"));
        answer = UDPClient.sendPacket(new Message(Message.messageTypes.MSG_RESOLVE_REQUEST, "root.home"));

        UDPClient.close();

        message = Message.readFromBytes(answer.getData());

        assertEquals(5, message.getMessageType());
        assertEquals("home 127.0.0.1 80", message.getPayload());

        // send "exit" to cli of server thread
        serverThread.interrupt();
    }

    @Test
    void clientRegisterTest() throws IOException, InterruptedException {
        Thread serverThread = setupServer(5566);

        DatagramPacket answer;
        Message message;

        UDPClient UDPClient = new UDPClient("localhost", 5566);

        answer = UDPClient.sendPacket(new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "root.home 127.0.0.1 80"));

        UDPClient.close();

        message = Message.readFromBytes(answer.getData());
        assertEquals(2, message.getMessageType());
        assertEquals("home", message.getPayload());

        // send "exit" to cli of server thread
        serverThread.interrupt();
    }
}