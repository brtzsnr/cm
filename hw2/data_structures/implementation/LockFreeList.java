package data_structures.implementation;

import java.util.concurrent.atomic.AtomicMarkableReference;
import data_structures.Sorted;


public class LockFreeList<T extends Comparable<T>> implements Sorted<T> {
  private class Node {
    public AtomicMarkableReference<Node> next = null;
    public int key;   // -1 head, 0 node, +1 tail. used for comparisons
    public T data = null;

    public Node(Node next, int key, T data) {
      this.next = new AtomicMarkableReference(next, false);
      this.key = key;
      this.data = data;
    }
  }

  private class Window {
    public Node pred, curr;

    public Window(Node pred, Node curr) {
      this.pred = pred;
      this.curr = curr;
    }
  }

  private Node tail = new Node(null, +1, null);
  private Node head = new Node(tail, -1, null);

  /*
   * The book requires that we have min/max in head/tail.
   * To achieve this for any data type we use an extra
   * key for comparison.
   */
  private int compare(Node node, T t) {
    if (node.key == 0) {
      return node.data.compareTo(t);
    }
    return node.key;
  }

  /*
   * Algorithm adapted from figure 9.24 from the book.
   */
  private Window find(Node head, T t) {
    Node pred, curr, succ;
    boolean[] marked = { false };
    boolean snip;

retry:
    while (true) {
      pred = head;
      curr = pred.next.getReference();

      while (true) {
        succ = curr.next.get(marked);

        while (marked[0]) {
          snip = pred.next.compareAndSet(curr, succ, false, false);
          if (!snip) {
            continue retry;
          }
          curr = succ;
          succ = curr.next.get(marked);
        }

        if (compare(curr, t) >= 0) {
          return new Window(pred, curr);
        }
        pred = curr;
        curr = succ;
      }
    }
  }

  /*
   * Algorithm adapted from figure 9.25 from the book.
   */
	public void add(T t) {
		while (true) {
      Window window = find(head, t);
      Node pred = window.pred, curr = window.curr;
      Node node = new Node(curr, 0, t);
      if (pred.next.compareAndSet(curr, node, false, false)) {
        break;
      }
    }
	}

  /*
   * Algorithm adapted from figure 9.26 from the book.
   */
	public void remove(T t) {
    while (true) {
      Window window = find(head, t);
      Node pred = window.pred, curr = window.curr;
      if (compare(curr, t) != 0) {
        System.err.println(t + " not found");
        return;
      }
      Node succ = curr.next.getReference();
      if (curr.next.attemptMark(succ, true)) {
        pred.next.compareAndSet(curr, succ, false, false);
        return;
      }
    }
	}

	public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("{ ");
    Node curr = head.next.getReference();
    while (curr != tail) {
      buffer.append(curr.data);
      buffer.append(", ");
      curr = curr.next.getReference();
    }
    buffer.append("}");
    return buffer.toString();
	}
}
