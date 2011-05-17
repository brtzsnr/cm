package data_structures.implementation;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import data_structures.Sorted;

public class FineGrainedList<T extends Comparable<T>> implements Sorted<T> {
  private class Node {
    public Lock lock = new ReentrantLock();
    public Node next = null;
    public T data = null;

    public Node(Node next, T data) {
      this.next = next;
      this.data = data;
    }
  }

  // head marks the start of the list, but it is not in the list.
  private Node head = new Node(null, null);

  public void add(T t) {
    Node curr = head;
    curr.lock.lock();

    Node next;
    while ((next = curr.next) != null) {
      // Invariant: curr is locked
      // Invariant: curr.data < t
      next.lock.lock();

      if (t.compareTo(next.data) <= 0) {
        break;
      }

      curr.lock.unlock();
      curr = next;
    }

    // curr is acquired
    curr.next = new Node(curr.next, t);
    if (curr.next.next != null) {  // old curr's next is acquired
      curr.next.next.lock.unlock();
    }
    curr.lock.unlock();
  }

  public void remove(T t) {
    Node curr = head;
    curr.lock.lock();

    while (curr.next != null) {
      curr.next.lock.lock();

      int r = t.compareTo(curr.next.data);
      if (r == 0) {
        Node tmp = curr.next.next;
        curr.next.lock.unlock();
        curr.next = tmp;
        break;
      } else if (r < 0) {
        curr.next.lock.unlock();
        break;
      }

      Node tmp = curr.next;
      curr.lock.unlock();
      curr = tmp;
    }

    curr.lock.unlock();
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
