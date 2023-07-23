import communication.format.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageTest {

    @Test
    void getBytes() {
        Message msg = new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "root.home 127.0.0.1 80");
        byte[] bytes = msg.getBytes();
        assertEquals("1", String.valueOf((char) bytes[0]));
        assertEquals("root.home 127.0.0.1 80", new String(bytes, 1, bytes.length - 1));
    }

    @Test
    void readFromBytes() {
        Message msg = new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "root.home 127.0.0.1 80");
        byte[] bytes = msg.getBytes();
        Message msg2 = Message.readFromBytes(bytes);
        assertEquals(msg.getMessageType(), msg2.getMessageType());
        assertEquals(msg.getPayload(), msg2.getPayload());
    }

    @Test
    void getPayload() {
        Message msg = new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "root.home 127.0.0.1 80");
        assertEquals("root.home 127.0.0.1 80", msg.getPayload());
    }

    @Test
    void getMessageType() {
        Message msg = new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "root.home 127.0.0.1 80");
        assertEquals(1, msg.getMessageType());
    }

    @Test
    void getMessageTypeEnum() {
        Message msg = new Message(Message.messageTypes.MSG_REGISTER_REQUEST, "root.home 127.0.0.1 80");
        assertEquals(Message.messageTypes.MSG_REGISTER_REQUEST, msg.getMessageTypeEnum(1));
    }

    @Test
    void getMessageTypeSize() {
        assertEquals(1, Message.messageTypes.getMessageTypeSize());
    }
}
