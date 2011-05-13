package data_structures.implementation;

import data_structures.Sorted;

public class CoarseGrainedTree<T extends Comparable<T>> implements Sorted<T> {
        public class Node {
                public Node left = null;
                public Node right = null;
                public T data;

                public Node(T data) {
                        this.data = data;
                }
        }

        private Node root = null;

        public synchronized void add(T t) {
                if (root == null) {
                        root = new Node(t);
                        return;
                }

                // Puts t into a leaf.
                Node curr = root;
                while (true) {
                        if (t.compareTo(curr.data) <= 0) {
                                if (curr.left == null) {
                                        curr.left = new Node(t);
                                        return;
                                } else {
                                        curr = curr.left;
                                }
                        } else {
                                if (curr.right == null) {
                                        curr.right = new Node(t);
                                        return;
                                } else {
                                        curr = curr.right;
                                }
                        }
                }
        }

        public synchronized void remove(T t) {
                // Finds the node containing t.
                Node last = null, curr = root;
                while (curr != null) {
                        int r = t.compareTo(curr.data);
                        if (r < 0) {
                                last = curr;
                                curr = curr.left;
                        } if (r > 0) {
                                last = curr;
                                curr = curr.right;
                        } else {
                                break;
                        }
                }

                if (curr == null) {
                        // t not found.
                        return;
                }

                // Merges curr's children.
                if (curr.left != null && curr.right != null) {
                        Node ptr = curr;
                        while (ptr.right != null) {
                                ptr = ptr.right;
                        }
                        ptr.right = curr.right;
                        curr.right = null;
                }

                if (curr.left == null) {
                        curr.left = curr.right;
                        curr.right = null;
                }

                // curr.left contains the merged tree for the two children
                // curr.right is always null
                if (last == null) {  // curr is the root
                        root = curr.left;
                } else {  // curr is an internal node
                        // Replaces curr in last with new merged tree.
                        if (last.left == curr) {
                                last.left = curr.left;
                        } else {
                                assert last.right == curr;
                                last.right = curr.left;
                        }
                }
        }

        private void toString(Node node, StringBuffer buffer) {
                if (node != null) {
                        toString(node.left, buffer);
                        buffer.append(node.data);
                        buffer.append(", ");
                        toString(node.right, buffer);
                }
        }

        public synchronized String toString() {
                StringBuffer buffer = new StringBuffer();
                buffer.append("{ ");
                toString(root, buffer);
                buffer.append("}");
                return buffer.toString();
        }
}