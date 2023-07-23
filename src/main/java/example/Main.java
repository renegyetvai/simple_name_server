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
                DatagramPacket answerOne, answerTwo, answerThree, answerFour;
                Message[] messages = new Message[4];

                System.out.println("Starting client");
                UDPClient UDPClient = new UDPClient("localhost", 5555);

                System.out.println("Sending request message");
                answerOne = UDPClient.sendPacket(new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "home\\local 1.2.3.4 8091"));
                answerTwo = UDPClient.sendPacket(new Message(Message.messageTypes.MSG_RESOLVE_REQUEST, "home\\local"));
                answerThree = UDPClient.sendPacket(new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "rg\\home\\local 1.2.3.5 8092"));
                answerFour = UDPClient.sendPacket(new Message(Message.messageTypes.MSG_RESOLVE_REQUEST, "rg\\home\\local"));

                UDPClient.close();

                // Convert the received message from a byte array to a message object.
                messages[0] = Message.readFromBytes(answerOne.getData());
                messages[1] = Message.readFromBytes(answerTwo.getData());
                messages[2] = Message.readFromBytes(answerThree.getData());
                messages[3] = Message.readFromBytes(answerFour.getData());

                for (Message message : messages) {
                    if (message == null) {
                        System.out.println("Message is null");
                        continue;
                    }
                    System.out.println("Received answer: " + "type = " + message.getMessageType() + " & content = '" + message.getPayload() + "'");
                }
                System.out.println("Client 1 done");
            }
            default -> System.out.println("Provided parameter was not found!\n " +
                    "Please enter parameter '-s' to start as server or '-c' to start as client!");
        }
    }
}
