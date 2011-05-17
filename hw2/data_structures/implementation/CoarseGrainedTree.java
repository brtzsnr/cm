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

  // lock protects whole tree
  private Lock lock = new ReentrantLock();
  // root is a hidden node containing no data.
  // real root is at the right of root
  private Node root = new Node(null);

  public void add(T t) {
    try {
      lock.lock();
      if (root.right == null) {
        root.right = new Node(t);
        return;
      }

      // Puts t into a leaf.
      Node curr = root.right;
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

  private void replace(Node parent, Node child, Node newChild) {
    assert parent.left == child || parent.right == child;

    if (parent.left == child) {
      parent.left = newChild;
    } else {
      parent.right = newChild;
    }
  }

  public synchronized void remove(T t) {
    try {
      lock.lock();
      // Finds the node containing t.
      Node last = root, curr = last.right;
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
      if (ptr.right == null) {
        ptr.right = curr.right;
        replace(last, curr, ptr);
        return;
      }

      while (ptr.right.right != null) {
        ptr = ptr.right;
      }

      // Invariant: ptr.right.right is null
      // Moves ptr.right at top of subtree
      // and reconnects ptr.right.left.
      curr.data = ptr.right.data;
      ptr.right = ptr.right.left;
    } finally {
      lock.unlock();
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
