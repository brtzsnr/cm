package data_structures.implementation;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import data_structures.Sorted;

public class FineGrainedTree<T extends Comparable<T>> implements Sorted<T> {
  public class Node {
    private Lock lock = new ReentrantLock();
    public Node left = null;
    public Node right = null;
    public T data;

    public Node(T data) {
      this.data = data;
    }

    public void lock() {
      lock.lock();
    }

    public void unlock() {
      lock.unlock();
    }
  }

  // root is a hidden node containing no data.
  // real root is at the right of root
  private Node root = new Node(null);;

  public void add(T t) {
    Node curr = root;

    try {
      curr.lock();
      if (curr.right == null) {
        curr.right = new Node(t);
        return;
      } else {
        Node tmp = curr.right;
        tmp.lock();
        curr.unlock();
        curr = tmp;
      }

      // Puts t into a leaf.
      // curr is always ackquired.
      while (true) {
        Node tmp;

        if (t.compareTo(curr.data) <= 0) {
          if (curr.left == null) {
            curr.left = new Node(t);
            return;
          } else {
            tmp = curr.left;
          }
        } else {
          if (curr.right == null) {
            curr.right = new Node(t);
            return;
          } else {
            tmp = curr.right;
          }
        }

        // Moves to the next node.
        tmp.lock();
        curr.unlock();
        curr = tmp;
      }
    } finally {
      curr.unlock();
    }
  }

  private void replace(Node parent, Node child, Node newChild) {
    assert parent.left == child || parent.right == child;

    if (parent.left == child) {
      parent.left = newChild;
    } else {
      parent.right = newChild;
    }
  }

  public void remove(T t) {
    Node last = root;
    Node curr = null;

    try {
      last.lock();
      curr = last.right;
      curr.lock();

      // Finds node containing t
      while (curr != null) {
        // Invariant: last and curr locks are aquired
        Node tmp = null;
        int r = t.compareTo(curr.data);
        if (r < 0) {
          tmp = curr.left;
        } else if (r > 0) {
          tmp = curr.right;
        } else {
          break;
        }

        // Moves to the next node
        last.unlock();
        tmp.lock();
        last = curr;
        curr = tmp;
      }

      if (curr == null) {
        // t not found.
        System.err.println(t + " not found");
        return;
      }

      if (curr.left == null && curr.right == null) {
        replace(last, curr, null);
        return;
      }
      if (curr.left != null && curr.right == null) {
        replace(last, curr, curr.left);
        return;
      }
      if (curr.left == null && curr.right != null) {
        replace(last, curr, curr.right);
        return;
      }

      // Merges curr's children.
      // Finds rightmost child in left child and replaces curr.
      Node ptr = curr.left;

      try {
        ptr.lock();
        if (ptr.right == null) {
          ptr.right = curr.right;
          replace(last, curr, ptr);
          return;
        }

        ptr.right.lock();
        while (ptr.right.right != null) {
          Node tmp = ptr.right;
          tmp.right.lock();
          ptr.unlock();
          ptr = tmp;
        }

        // Invariant: ptr is locked
        // Invariant: ptr.right is locked
        // Invariant: ptr.right.right is null
        // Moves ptr.right at top of subtree
        // and reconnects ptr.right.left.
        Node tmp = ptr.right;
        curr.data = ptr.right.data;
        ptr.right = ptr.right.left;
        tmp.unlock();
      } finally {
        ptr.unlock();
      }
    } finally {
      last.unlock();
      if (curr != null) {
        curr.unlock();
      }
    }
  }

  private void toString(Node node, StringBuffer buffer, int level) {
    if (node != null) {
      toString(node.left, buffer, level + 1);

      for (int i = 0; i < level; i++) {
        buffer.append("  ");
      }
      buffer.append(node.data);
      buffer.append("\n");

      toString(node.right, buffer, level + 1);
    }
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("\n");
    toString(root.right, buffer, 0);
    return buffer.toString();
  }
}
