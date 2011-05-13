package data_structures.implementation;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
        private Lock lock = new ReentrantLock();

        public void add(T t) {
                try {
                        lock.lock();
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
                } finally {
                        lock.unlock();
                }
        }

        public synchronized void remove(T t) {
                try {
                        lock.lock();
                        // Finds the node containing t.
                        Node last = null, curr = root;
                        while (curr != null) {
                                int r = t.compareTo(curr.data);
                                if (r < 0) {
                                        last = curr;
                                        curr = curr.left;
                                } else if (r > 0) {
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
                        assert t.compareTo(curr.data) == 0;
                        if (curr.left != null && curr.right != null) {
                                Node ptr = curr.left;
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

                        // curr.left contains the merged tree of children
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
                } finally {
                        lock.unlock();
                }
        }

        private void toString(Node node, StringBuffer buffer, int level) {
                if (node != null) {
                        for (int i = 0; i < level; i++) {
                                buffer.append("  ");
                        }
                        buffer.append(node.data);
                        buffer.append("\n");
                        toString(node.left, buffer, level + 1);
                        toString(node.right, buffer, level + 1);
                }
        }

        public synchronized String toString() {
                if (root == null) {
                        return "";
                }

                StringBuffer buffer = new StringBuffer();
                buffer.append("\n");
                toString(root, buffer, 0);
                return buffer.toString();
        }
}
