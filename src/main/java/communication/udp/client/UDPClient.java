package communication.udp.client;

import communication.format.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

    private static final int UDP_BUFFER_SIZE = 512;
    private final InetAddress serverAddress;
    private final int port;
    private final DatagramSocket clientSocket;

    /*
    --> Use the following constructor if you want to specify a port:
    */
    public UDPClient(String serverAddress, int port) throws IOException {
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.port = port;
        this.clientSocket = new DatagramSocket();
    }

    /**
     * Sends a message to the server.
     * @param msg The message to send.
     * @throws IOException If the message could not be sent.
     */
    public synchronized DatagramPacket sendPacket(Message msg) throws IOException {
        byte[] sendBuffer = msg.getBytes();
        DatagramPacket request = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, port);
        clientSocket.send(request);

        byte[] receiveBuffer = new byte[UDP_BUFFER_SIZE];
        DatagramPacket response = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        clientSocket.receive(response);
        return response;
    }

    /**
     * Closes the socket.
     */
    public void close() {
        clientSocket.close();
    }
}
