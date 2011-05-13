package data_structures.implementation;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import data_structures.Sorted;

public class FineGrainedList<T extends Comparable<T>> implements Sorted<T> {
        private class Node {
                public Node next = null;
                public T data = null;
                private Lock lock = new ReentrantLock();

                public Node(Node next, T data) {
                        this.next = next;
                        this.data = data;
                }

                public void lock() {
                        lock.lock();
                }

                public void unlock() {
                        lock.unlock();
                }
        }

        // head marks the start of the list, but it is not in the list.
        private Node head = new Node(null, null);

        public void add(T t) {
                Node curr = head;
                curr.lock();

                while (curr.next != null) {
                        curr.next.lock();

                        if (t.compareTo(curr.next.data) <= 0) {
                                break;
                        }

                        Node tmp = curr.next;
                        curr.unlock();
                        curr = tmp;
                }

                // curr is acquired
                curr.next = new Node(curr.next, t);
                if (curr.next.next != null) {  // old curr's next is acquired
                        curr.next.next.unlock();
                }
                curr.unlock();
        }

        public void remove(T t) {
                Node curr = head;
                curr.lock();

                while (curr.next != null) {
                        curr.next.lock();

                        if (t.compareTo(curr.next.data) == 0) {
                                Node tmp = curr.next.next;
                                curr.next.unlock();
                                curr.next = tmp;
                                break;
                        }

                        Node tmp = curr.next;
                        curr.unlock();
                        curr = tmp;
                }

                curr.unlock();
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
