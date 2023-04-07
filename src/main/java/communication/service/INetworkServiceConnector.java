package communication.service;

import java.net.UnknownHostException;

public interface INetworkServiceConnector {

    // Uses the implementation of the service
    void registerService(INetworkService service) throws UnknownHostException;

    // Returns the name of the service
    String getServiceName();

    // Returns the port of the service
    int getServicePort();

    // Returns the IP of the service
    String getServiceIP();

}
