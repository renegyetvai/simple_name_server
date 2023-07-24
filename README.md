# simple_name_server
This repository contains an example program for the implementation of a UDP based nameserver. As reference implementation of the client and server I used my before written UDP server and client from the [UDP server](https://github.com/renegyetvai/udp_server) project. Both the client and the server communicate using a simple message format. Simple tests for checking the source code are included under src/test/java/.

## Usage
To run the source code, the repository must be cloned and the project it contains opened as a Gradle project. Once this is done, first execute ```gradlew build``` and then ```gradlew run --args="-c"``` (for the client) or ```gradlew run --args="-s"``` (for the server) can be used to run the sample program. To quit the server enter ```exit``` into the CLI of the running server session. The client program quits automatically after finishing.

## Message format
The message format implemented for communication uses the first byte to indicate the message type and then appends all other bytes containing the message.
The structure of the format looks like this:
```
 0      7 8     15 16    23 24    31
+--------+--------+--------+--------+
|  Type  |          Content ...     
+--------+--------+--------+----- ...
```

### Message types
The message format includes the following message types:
```
MSG_REGISTER_REQUEST, MSG_REGISTER_REPLY, MSG_REGISTER_ERROR, 
MSG_RESOLVE_REQUEST, MSG_RESOLVE_REPLY, MSG_RESOLVE_ERROR, 
MSG_DELETE_REQUEST, MSG_DELETE_REPLY, MSG_DELETE_ERROR, 
MSG_ERROR
```

### Message content
To register a new name at the server we use a message like: 
```
Type: MSG_REGISTER_REQUEST; Message: <name>, <ip>, <port> 
The name and ip are strings and the port is an integer.
```

To resolve a name we use a message like: 
```
Type: MSG_RESOLVE_REQUEST; Message: <name>
The name is a string.
```

To delete a name we use a message like: 
```
Type: MSG_DELETE_REQUEST; Message: <name>
The name is a string.
```