package communication.udp.server;

import communication.format.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class UDPServer implements Runnable {

    private final byte[] buffer = new byte[512];
    private final int port;
    private DatagramSocket serverSocket;
    private Boolean terminate = false;

    public UDPServer(int port) {
        this.port = port;
    }

    /**
     * Manages the servers accepter thread and listens for a termination message to shut everything down.
     */
    private void processManager() throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        // Create thread to handle incoming messages
        Thread accepterThread = new Thread (this::accepter);
        accepterThread.start();

        // Wait for termination signal
        while (!terminate) {
            String input = scanner.nextLine();
            if (input.equals("exit")) {
                terminate = true;
            } else {
                System.err.println(input);
            }
        }
        scanner.close();

        // Kill accepterThread
        try {
            Thread.sleep(1);
            accepterThread.interrupt();
            Thread.sleep(5);
        } catch (Exception e) {
            System.err.println("Caught: " + e);
        }
        System.err.println("Closed accepterThread");

        // Check if serverSocket is still open
        if (!serverSocket.isClosed()) {
            serverSocket.close();
            System.err.println("Server socket is closed!");
        }
    }

    private void accepter() {
        System.err.println("Server initialized successfully! Now listening...");

        // Create loop to handle incoming messages
        while (!Thread.interrupted()) {
            try {
                handler();
            } catch (RuntimeException e) {
                if (!(e.getCause() instanceof SocketException)) {
                    System.err.println(e.getMessage());
                }
            }

            if (serverSocket.isClosed() || Thread.interrupted()) {
                break;
            }

            serverSocket.close();
            System.err.println("Server socket closed!");
            createServerSocket(this.port);
            System.err.println("Server ready for new connections...");
        }
    }

    private void handler() {
        while (!Thread.currentThread().isInterrupted()) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Set length of packet to maximum size
            packet.setLength(buffer.length);

            try {
                serverSocket.receive(packet);

                // Routine Service receive
                process(packet);

                // Reset packet and send message with type MSG_REPLY back to client
                packet.setAddress(packet.getAddress());
                packet.setPort(packet.getPort());
                packet.setLength(buffer.length);
                packet.setData(new Message(Message.messageTypes.MSG_RESOLVE_REQUEST, "reply").getBytes());

                serverSocket.send(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Clean buffer
            Arrays.fill(buffer, (byte) 0);
        }
    }

    private void process(DatagramPacket packet) {
        // create message from packet
        byte[] data = packet.getData();
        Message msg = Message.readFromBytes(data);

        // print info
        System.out.println("Received message type: " + msg.getMessageType());
        System.out.println("Received message payload: " + msg.getPayload());
    }

    /**
     * Creates the server socket.
     * @param port The port to listen on.
     */
    private void createServerSocket(int port) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        System.err.println("Creating server socket...");

        // Create UDP socket
        try {
            this.serverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        System.err.println("Server socket created on port " + port);
    }

    public DatagramSocket getServerSocket() {
        return serverSocket;
    }

    @Override
    public void run() {
        System.err.println("Starting server...");

        // Routine
        try {
            this.createServerSocket(this.port);
            this.processManager();
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        } catch (NoSuchElementException e) {
            System.err.println("CLI not available!");
        }

        System.err.println("Shutting down server...");
    }
}