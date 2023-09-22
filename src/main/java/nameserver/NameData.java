package nameserver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Stores Node information inside a Tree.
 */
public class NameData {

    public enum removeType {
        FORCE,
        NORMAL
    }

    private final HashMap<String, Node> nodeMap;
    private final Node root;

    public NameData(String rootName) {
        nodeMap = new HashMap<>();
        root = new Node(rootName, new HashMap<>());
        nodeMap.put(root.getFullName(), root);
    }

    public static Node getRoot(NameData nameData) {
        return nameData.root;
    }

    public String addNode(String name, String ip, int port) {
        // Split the name into its parts.
        String[] nameParts = name.split("\\.");

        // Get the node that the name belongs to.
        Node parent = null;
        if (nameParts.length != 1) {
            // Create new node for the parent and its parents.
            int cnt = 1;
            String parentName = getRoot(this).getFullName();
            String oldParentName = getNode(parentName).getFullName();

            while (nameParts.length > cnt) {

                parentName = nameParts[cnt - 1];
                parent = getNode(parentName);

                if (parent == null) {
                    parent = new Node(parentName, new HashMap<>());
                    parent.setNodeType(Node.NodeType.NODE);

                    if (cnt - 1 == 0) {
                        parent.setParent(root);
                    } else {
                        parent.setParent(getNode(oldParentName));
                    }

                    parent.getParent().setChildren(parent.getFullName(), parent);
                    nodeMap.put(parent.getFullName(), parent);
                }
                if (parent.getNodeType() == Node.NodeType.LEAF) {
                    parent.setNodeType(Node.NodeType.NODE);
                }

                oldParentName = parent.getFullName();
                cnt++;
            }
        }

        // Create a new node.
        Node newNode = new Node(nameParts[nameParts.length - 1], ip, port, parent, this);

        // Add the new node to the node map.
        nodeMap.put(newNode.getFullName(), newNode);

        return newNode.getName();
    }

    public Node getNode(String name) {
        if(nodeMap.containsKey(name)) {
            return nodeMap.get(name);
        }
        else {
            return dfs(root, name);
        }
    }

    public Boolean removeNode(String fullName, removeType removeType) {
        Node node = nodeMap.get(fullName);
        if (node == null) {
            return false;
        }
        if (removeType == NameData.removeType.FORCE) {
            nodeMap.remove(fullName);
            removeNodeFromParentsChildren(node);
            return true;
        } else if (removeType == NameData.removeType.NORMAL) {
            if (node.getNodeType() == Node.NodeType.LEAF) {
                nodeMap.remove(fullName);
                removeNodeFromParentsChildren(node);
                return true;
            } else if (node.getNodeType() == Node.NodeType.NODE) {
                nodeMap.remove(fullName);
                for (Node child : node.getChildren()) {
                    // rename key in nodeMap for child
                    nodeMap.remove(child.getFullName());
                    nodeMap.put(node.getParent().getName() + "." + child.getName(), child);
                    // set parent of child to parent of node and add child to parent's children
                    child.setParent(node.getParent());
                    node.getParent().addChild(child.getName(), child);
                }
                removeNodeFromParentsChildren(node);
                return true;
            }
        }
        return false;
    }

    private void removeNodeFromParentsChildren(Node node) {
        // Remove the node from the parent's children. Then set the parent's children to the new children.
        List<Node> children = new java.util.ArrayList<>(Arrays.stream(node.getParent().getChildren()).toList());
        children.remove(node);
        node.getParent().setChildren(children);
    }

    // Deep first search to find a node with the given name.
    private Node dfs(Node node, String name) {
        if(node.getName().equals(name)) {
            return node;
        }

        if(node.getChildren() != null) {
            for(Node child : node.getChildren()) {
                Node found = dfs(child, name);
                if(found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}