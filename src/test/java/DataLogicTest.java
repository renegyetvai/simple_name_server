import nameserver.NameData;
import nameserver.Node;
import org.junit.jupiter.api.Test;

import static nameserver.NameData.getRoot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DataLogicTest {

    @Test
    public void testNodeProperties() {
        NameData testNameData = new NameData("root");
        Node root = getRoot(testNameData);
        testNameData.addNode("testChild", "127.0.0.1", 55);
        Node child = testNameData.getNode("testChild");

        // first tests
        assertEquals(root, child.getParent());
        assertEquals(Node.NodeType.ROOT, root.getNodeType());
        assertEquals("testChild", child.getName());
        assertEquals("127.0.0.1", child.getIp());
        assertEquals(55, child.getPort());
        assertEquals(Node.NodeType.LEAF, child.getNodeType());
        assertEquals("root", root.getFullName());
        assertEquals("root.testChild", child.getFullName());

        testNameData.addNode("testChild.testLeafOne", "127.0.0.2", 80);
        testNameData.addNode("testChild.testLeafTwo", "127.0.0.3", 80);
        child = testNameData.getNode("testChild");
        Node leafOne = testNameData.getNode("root.testChild.testLeafOne");
        Node leafTwo = testNameData.getNode("root.testChild.testLeafTwo");

        // second tests
        assertEquals(Node.NodeType.NODE, child.getNodeType());
        assertEquals(Node.NodeType.LEAF, leafOne.getNodeType());
        assertEquals(Node.NodeType.LEAF, leafTwo.getNodeType());
        assertEquals(leafOne, testNameData.getNode("root.testChild.testLeafOne"));
    }

    @Test
    public void testNodeMap() {
        NameData testNameData = new NameData("root");
        testNameData.addNode("testChild", "127.0.0.1", 55);
        testNameData.addNode("testChild.testLeafOne", "127.0.0.2", 80);
        testNameData.addNode("testChild.testLeafTwo", "127.0.0.3", 80);

        // first tests
        assertEquals(1, testNameData.getNode("root").getChildren().length);
        assertEquals(2, testNameData.getNode("root.testChild").getChildren().length);
        assertNull(testNameData.getNode("root.testChild.testLeafOne").getChildren());
        assertNull(testNameData.getNode("root.testChild.testLeafTwo").getChildren());

        // remove leaf one and test again
        testNameData.removeNode("root.testChild.testLeafOne", NameData.removeType.NORMAL);
        assertEquals(1, testNameData.getNode("root").getChildren().length);
        assertEquals(2, testNameData.getNode("root.testChild").getChildren().length);
        assertNull(testNameData.getNode("root.testChild.testLeafTwo").getChildren());

        // remove child and test again
        testNameData.removeNode("root.testChild", NameData.removeType.NORMAL);
        assertEquals(1, testNameData.getNode("root").getChildren().length);
        assertNull(testNameData.getNode("root.testChild.testLeafTwo").getChildren());
    }
}
