package nameserver;

import java.util.Arrays;
import java.util.HashMap;

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
        if (!(nameParts.length == 1)) {
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

        // Add the new node to the children of the parent.
        if (parent != null) {
            parent.setChildren(parent.getFullName() + "." + nameParts[0], newNode);
        }

        // Add the new node to the node map.
        nodeMap.put(newNode.getFullName(), newNode);

        return newNode.getName();
    }

    public Node getNode(String name) {
        // Check if the node exists otherwise traverse the tree to find the node.
        if (nodeMap.containsKey(name)) {
            return nodeMap.get(name);
        } else {
            // Check if the node is the root node.
            if (nodeMap.get(root.getFullName()).getName().equals(name)) {
                return root;
            } else {
                // Traverse into each branch of the tree beginning from root and if we found the node inside return its full name.
                Node currentNode = nodeMap.get(root.getFullName());
                Node[] children = currentNode.getChildren();
                while (true) {

                    for (Node child : children) {
                        if (child.getName().equals(name)) {
                            return child;
                        }
                    }

                    // Check if any children exist otherwise return null since the searched node can't exist.
                    if (currentNode == root && children.length == 0) {
                        return null;
                    }

                    int cnt = 0;
                    for (Node child : children) {
                        if (!(child.getChildren() == null)) {
                            cnt++;
                        }
                        // If there are no children left, return null.
                        if (cnt > 0) {
                            return null;
                        }
                    }

                    // Get the next children.
                    Node[] newChildren = new Node[0];
                    Node[] tmp = new Node[0];

                    for (Node child : children) {
                        // Expand array to the current amount + the number of children of the current child.
                        tmp = Arrays.copyOf(tmp, tmp.length + child.getChildren().length);

                        // Copy existing children in newChildren to the expanded array.
                        System.arraycopy(newChildren, 0, tmp, 0, newChildren.length);

                        // Add the new children to the expanded array.
                        System.arraycopy(child.getChildren(), 0, tmp, newChildren.length, child.getChildren().length);

                        newChildren = tmp;
                    }
                    children = newChildren;
                }
            }
        }
    }

    public Boolean removeNode(String fullName, removeType removeType) {
        Node node = nodeMap.get(fullName);
        if (node == null) {
            return false;
        }
        if (removeType == NameData.removeType.FORCE) {
            nodeMap.remove(fullName);
            return true;
        } else if (removeType == NameData.removeType.NORMAL) {
            if (node.getNodeType() == Node.NodeType.LEAF) {
                nodeMap.remove(fullName);
                return true;
            } else if (node.getNodeType() == Node.NodeType.NODE) {
                nodeMap.remove(fullName);
                for (Node child : node.getChildren()) {
                    removeNode(child.getFullName(), NameData.removeType.NORMAL);
                }
                return true;
            }
        }
        return false;
    }

    // Method that traverses the tree and returns the child nodes of the given node.
    public Node[] getChildren(String fullName) {
        // Check if the full name exists, otherwise use root.
        if (!nodeMap.containsKey(fullName)) {
            return new Node[]{root};
        } else {
            return nodeMap.get(fullName).getChildren();
        }
    }
}