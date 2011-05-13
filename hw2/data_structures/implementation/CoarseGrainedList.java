package data_structures.implementation;

import data_structures.Sorted;

public class CoarseGrainedList<T extends Comparable<T>> implements Sorted<T> {
        private class Node {
                public Node next = null;
                public T data = null;

                public Node(Node next, T data) {
                        this.next = next;
                        this.data = data;
                }
        }

        // head marks the start of the list, but it is not in the list.
        private Node head = new Node(null, null);

        public synchronized void add(T t) {
                Node curr = head;
                while (curr.next != null) {
                        if (t.compareTo(curr.next.data) <= 0) {
                                break;
                        }
                        curr = curr.next;
                }
                curr.next = new Node(curr.next, t);
        }

        public synchronized void remove(T t) {
                Node curr = head;
                while (curr.next != null) {
                        if (t.compareTo(curr.next.data) == 0) {
                                curr.next = curr.next.next;
                                break;
                        }
                        curr = curr.next;
                }
        }

        public String toString() {
                StringBuffer buffer = new StringBuffer();
                buffer.append("{ ");
                Node curr = head.next;
                while (curr != null) {
                        buffer.append(curr.data);
                        buffer.append(", ");
                        curr = curr.next;
                }
                buffer.append("}");
                return buffer.toString();
        }
}
