package nameserver;

import communication.service.Service;

import java.io.IOException;

public interface INameService {
    Service resolveName(String name) throws IOException;
    String registerName(String name, String ip, int port) throws IOException;
    Boolean registerName(Service service) throws IOException;
}