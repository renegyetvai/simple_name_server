package communication.service;

public class Service implements IService {
    private String name;
    private String ip;
    private int port;

    public Service(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public Service() {
        this.name = "not assigned";
        this.ip = "localhost";
        this.port = 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getIP() {
        return ip;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void setIP(String ip) {
        this.ip = ip;
    }
}
