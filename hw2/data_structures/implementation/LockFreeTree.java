package data_structures.implementation;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;
import data_structures.Sorted;

public class LockFreeTree<T extends Comparable<T>> implements Sorted<T> {
  private static final int CLEAN = 0;
  private static final int DFLAG = 1;
  private static final int IFLAG = 2;
  private static final int MARK  = 3;

  private class Node {
    public int key;   // 0, 1, 2 for node, infinity_1, infinity_2
    public T data;
    boolean internal;

    public Node(int key, T data) {
      this.key = key;
      this.data = data;
    }
  }

  private class Internal extends Node {
    public Update update;
    public AtomicReference<Node> left;
    public AtomicReference<Node> right;

    public Internal(int key, T data) {
      super(key, data);
      internal = true;
    }
  }

  private class Leaf extends Node {
    public Leaf(int key, T data) {
      super(key, data);
      internal = false;
    }
  }

  private class Update {
    public AtomicStampedReference<Info> update;

    public Update(Info info, int state) {
      update = new AtomicStampedReference(info, state);
    }

    public Update copy() {
      int[] state = { 0 };
      Info info = update.get(state);
      return new Update(info, state[0]);
    }

    public int getState() {
      return update.getStamp();
    }

    public Info getInfo() {
      return update.getReference();
    }

    public boolean cas(Info expectedInfo, int expectedState,
                       Info newInfo, int newState) {
      return update.compareAndSet(expectedInfo, newInfo,
                                  expectedState, newState);
    }
  }

  private class Info {
  }

  private class IInfo extends Info {
    public Internal p, newInternal;
    public Leaf l;
  }

  private class DInfo extends Info {
    public Internal gp, p;
    public Leaf l;
    public Update pupdate;
  }

  private class SearchResult {
    Internal gp, p;
    Leaf l;
    Update pupdate, gpupdate;

    public SearchResult(Internal gp, Internal p, Leaf l,
                        Update pupdate, Update gpupdate) {
      this.gp = gp;
      this.p = p;
      this.l = l;
      this.pupdate = pupdate;
      this.gpupdate = gpupdate;
    }
  }

  private Node root = null;

  public LockFreeTree() {
    Internal root_ = new Internal(2, null);
    root_.update = new Update(null, CLEAN);
    root_.left = new AtomicReference(new Leaf(1, null));
    root_.right = new AtomicReference(new Leaf(2, null));
    root = root_;
  }

  /*
   * Provides T with infinity_1 < infinity_2
   */
  private int compare(Node node, T t) {
    if (node.key == 0) {
      return node.data.compareTo(t);
    }
    return 1;
  }

  private int compare(Node n1, Node n2) {
    if (n1.key == 0 && n2.key == 0) {
      return n1.data.compareTo(n2.data);
    }
    return n1.key - n2.key;
  }

  /*
   * Algorithm adapted from paper, page 6
   */
  private SearchResult search(T t) {
    Internal gp = null, p = null;
    Node l = root;
    Update gpupdate = null;
    Update pupdate = null;

    while (l.internal) {
      gp = p;
      p = (Internal) l;

      // gpupdate and pupdate are copies
      gpupdate = pupdate;   // already a copy
      pupdate = p.update.copy();

      if (compare(l, t) > 0) {
        l = p.left.get();
      } else {
        l = p.right.get();
      }
    }

    return new SearchResult(gp, p, (Leaf) l, pupdate, gpupdate);
  }

  private void help(Update u) {
    if (u.getState() == IFLAG) {
      helpInsert((IInfo) u.getInfo());
    } else if (u.getState() == MARK) {
      helpMarked((DInfo) u.getInfo());
    } else if (u.getState() == DFLAG) {
      helpDelete((DInfo) u.getInfo());
    }
  }

  public void helpInsert(IInfo op) {
    casChild(op.p, op.l, op.newInternal);
    op.p.update.cas(op, IFLAG, op, CLEAN);
  }

  public boolean helpDelete(DInfo op) {
    op.p.update.cas(
        op.pupdate.getInfo(), op.pupdate.getState(), op, MARK);

    // This isn't what the paper says, but there is no way to do
    // read-compare-and-swap with AtomicStampedReference.
    if (op.p.update.getState() == MARK) {
      helpMarked((DInfo) op);
      return true;
    } else {
      help(op.p.update);
      op.gp.update.cas(op, DFLAG, op, CLEAN);
      return false;
    }
  }

  public void helpMarked(DInfo op) {
    Node other;

    if (op.p.right.get() == op.l) {
      other = op.p.left.get();
    } else {
      other = op.p.right.get();
    }

    casChild(op.gp, op.p, other);
    op.gp.update.cas(op, DFLAG, op, CLEAN);
  }

  public void casChild(Internal parent, Node old, Node newNode) {
    if (compare(newNode, parent) < 0) {
      parent.left.compareAndSet(old, newNode);
    } else {
      parent.right.compareAndSet(old, newNode);
    }
  }

	public void add(T t) {
    Internal newInternal;
    Leaf newSibling;
    Leaf newLeaf = new Leaf(0, t);

    while (true) {
      SearchResult sr = search(t);
      if (compare(sr.l, t) == 0) {
        return;
      }

      if (sr.pupdate.getState() != CLEAN) {
        help(sr.pupdate);
      } else {
        newSibling = new Leaf(sr.l.key, sr.l.data);
        if (compare(sr.l, t) > 0) {
          newInternal = new Internal(sr.l.key, sr.l.data);
          newInternal.update = new Update(null, CLEAN);
          newInternal.left = new AtomicReference(newLeaf);
          newInternal.right = new AtomicReference(newSibling);
        } else {
          newInternal = new Internal(0, t);
          newInternal.update = new Update(null, CLEAN);
          newInternal.left = new AtomicReference(newSibling);
          newInternal.right = new AtomicReference(newLeaf);
        }

        IInfo op = new IInfo();
        op.p = sr.p;
        op.newInternal = newInternal;
        op.l = sr.l;

        boolean result = sr.p.update.cas(
            sr.pupdate.getInfo(), sr.pupdate.getState(), op, IFLAG);

        if (result) {
          helpInsert(op);
          return;
        } else {
          help(sr.p.update);
        }
      }
    }
	}

	public void remove(T t) {
    while (true) {
      SearchResult sr = search(t);
      if (compare(sr.l, t) != 0) {
        System.err.println(t + " not found");
        return;
      }
      if (sr.gpupdate.getState() != CLEAN) {
        help(sr.gpupdate);
      } else if (sr.pupdate.getState() != CLEAN) {
        help(sr.pupdate);
      } else {
        DInfo op = new DInfo();
        op.gp = sr.gp;
        op.p = sr.p;
        op.l = sr.l;
        op.pupdate = sr.pupdate;

        boolean result = sr.gp.update.cas(
            sr.gpupdate.getInfo(), sr.gpupdate.getState(), op, DFLAG);

        if (result) {
          if (helpDelete(op)) {
            return;
          }
        } else {
          help(sr.gp.update);
        }
      }
    }
	}

  private void toString(Node node, StringBuffer buffer, int level) {
    if (node != null) {
      if (node.internal) {
        toString(((Internal) node).left.get(), buffer, level + 1);
      }

      for (int i = 0; i < level; i++) {
        buffer.append("  ");
      }
      buffer.append(node.data);
      if (!node.internal) {
        buffer.append("*");
      }
      buffer.append("\n");

      if (node.internal) {
        toString(((Internal) node).right.get(), buffer, level + 1);
      }
    }
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("\n");
    toString(root, buffer, 0);
    return buffer.toString();
  }
}
