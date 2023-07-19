package example;

import communication.format.Message;
import communication.udp.client.UDPClient;
import communication.udp.server.UDPServer;
import nameserver.NameService;

import java.io.IOException;
import java.net.DatagramPacket;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length < 1) {
            System.out.println("Missing arguments");
            System.exit(0);
        }

        switch (args[0]) {
            case "-s" -> {
                NameService serviceProvider = new NameService("local");
                Thread serverThread = new Thread(new UDPServer(5555, serviceProvider));
                System.out.println("Starting nameserver");
                serverThread.start();
                serverThread.join();
            }
            case "-c" -> {
                DatagramPacket answer1, answer2, answer3, answer4;
                Message message;

                System.out.println("Starting client");
                UDPClient UDPClient = new UDPClient("localhost", 5555);

                System.out.println("Sending request message");
                answer1 = UDPClient.sendPacket(new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "home\\local 1.2.3.4 8091"));
                answer2 = UDPClient.sendPacket(new Message(Message.messageTypes.MSG_RESOLVE_REQUEST, "home\\local"));
                answer3 = UDPClient.sendPacket(new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "rg\\home\\local 1.2.3.5 8092"));
                answer4 = UDPClient.sendPacket(new Message(Message.messageTypes.MSG_RESOLVE_REQUEST, "rg\\home\\local"));

                UDPClient.close();

                // Convert the received message from a byte array to a message object.
                message = Message.readFromBytes(answer1.getData());
                System.out.println("Received answer: " + "type = " + message.getMessageType() + " & content = '" + message.getPayload() + "'");
                System.out.println("Client 1 done");
            }
            default -> System.out.println("Provided parameter was not found!\n " +
                    "Please enter parameter '-s' to start as server or '-c' to start as client!");
        }
    }
}
