package communication.udp.server;

import communication.format.Message;
import service.IService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class UDPServer implements Runnable {

    private final byte[] buffer = new byte[512];
    private final IService serviceProvider;
    private final int port;
    private DatagramSocket serverSocket;
    private Boolean terminate = false;

    public UDPServer(int port, IService serviceProvider) {
        this.port = port;
        this.serviceProvider = serviceProvider;
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
        while (Boolean.TRUE.equals(!terminate)) {
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
            Thread.currentThread().interrupt();
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

                // Service provider processes the message
                Message answer = serviceProvider.process(packet);

                // Reset packet and send answer back to client
                packet.setAddress(packet.getAddress());
                packet.setPort(packet.getPort());
                packet.setLength(buffer.length);
                packet.setData(answer.getBytes());

                System.err.println("Sending answer to client...");
                serverSocket.send(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Clean buffer
            Arrays.fill(buffer, (byte) 0);
        }
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

    @Override
    public void run() {
        System.err.println("Starting UDP server...");

        // Routine
        try {
            this.createServerSocket(this.port);
            this.processManager();
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
            Thread.currentThread().interrupt();
        } catch (NoSuchElementException e) {
            System.err.println("CLI not available!");
        }

        System.err.println("Shutting down server...");
    }
}