package data_structures.implementation;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
        private Lock lock = new ReentrantLock();

        public void add(T t) {
                lock.lock();
                Node curr = head;
                while (curr.next != null) {
                        if (t.compareTo(curr.next.data) <= 0) {
                                break;
                        }
                        curr = curr.next;
                }
                curr.next = new Node(curr.next, t);
                lock.unlock();
        }

        public void remove(T t) {
                lock.lock();
                Node curr = head;
                while (curr.next != null) {
                        if (t.compareTo(curr.next.data) == 0) {
                                curr.next = curr.next.next;
                                break;
                        }
                        curr = curr.next;
                }
                lock.unlock();
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
