package nameserver;

import java.util.HashMap;
import java.util.List;

import static nameserver.NameData.getRoot;

public class Node {

    public enum NodeType {
        LEAF,
        NODE,
        ROOT
    }

    private final String name;
    private String ip;
    private int port;
    private NodeType nodeType;
    private Node parent = null;
    private HashMap<String, Node> children;

    public Node(String name, String ip, int port, Node parent, NameData nameData) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.nodeType = NodeType.LEAF;
        this.parent = parent;

        if (parent == null) {
            // Get the root node and set it as the parent.
            this.parent = getRoot(nameData);
            this.parent.children.put(this.getFullName(), this);
        } else {
            // Check if the parent is a node or a leaf. If it is a leaf, don't add it to the children.
            if (this.parent.nodeType != NodeType.LEAF) {
                if (this.parent.children == null) {
                    this.parent.children = new HashMap<>();
                }
                this.parent.children.put(this.getFullName(), this);
            } else {
                // Indicate that the node already exists.
                System.err.println("Node already exists!");
            }
        }
    }

    public Node(String name, HashMap<String, Node> children) {
        this.name = name;
        this.nodeType = NodeType.ROOT;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String getFullName() {
        if (parent == null) {
            return name;
        } else {
            return parent.getFullName() + "." + name;
        }
    }

    public Node[] getChildren() {
        if (children == null) {
            return null;
        } else {
            return children.values().toArray(new Node[0]);
        }
    }

    public void setChildren(String name, Node child) {
        if (children == null) {
            children = new HashMap<>();
        }
        children.put(name, child);
    }

    public void setChildren(List<Node> children) {
        if (this.children == null) {
            this.children = new HashMap<>();
        }
        for (Node child : children) {
            this.children.put(child.getFullName(), child);
        }
    }
}