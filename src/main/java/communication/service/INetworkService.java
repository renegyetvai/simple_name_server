package communication.service;

import communication.format.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public interface INetworkService extends IService {
        void send(InetAddress address, int port, Message msg) throws IOException;
        void receive(DatagramPacket packet) throws IOException;
}
