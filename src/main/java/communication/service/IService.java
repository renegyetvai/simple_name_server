package communication.service;

public interface IService {
    String getName();
    int getPort();
    String getIP();
    void setName(String name);
    void setPort(int port);
    void setIP(String ip);
}
