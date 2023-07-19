package service;

import communication.format.Message;

import java.net.DatagramPacket;

public interface IService {
    Message process(DatagramPacket packet);
}
