import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FineGrainedList<T> implements Set<T> {
    private Node head;

    // Node class with lock functionality
    private class Node {
        T item;
        int key;
        Node next;
        Lock lock;

        Node(T item) {
            this.item = item;
            this.key = item == null ? Integer.MIN_VALUE : item.hashCode(); // Use sentinel for head/tail
            this.lock = new ReentrantLock();
        }

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
        }
    }

    // Constructor initializes the list with sentinel nodes
    public FineGrainedList() {
        head = new Node(null); // -∞ sentinel
        head.next = new Node(null); // ∞ sentinel
    }

    @Override
    public boolean add(T item) {
        int key = item.hashCode();
        Node pred = null, curr = null;
        head.lock();
        try {
            pred = head;
            curr = pred.next;
            curr.lock();
            try {
                while (curr.key < key) {
                    pred.unlock(); // Unlock the predecessor
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (curr.key == key) {
                    return false; // Item already exists
                }
                Node newNode = new Node(item);
                newNode.next = curr;
                pred.next = newNode;
                return true;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    @Override
    public boolean remove(T item) {
        int key = item.hashCode();
        Node pred = null, curr = null;
        head.lock();
        try {
            pred = head;
            curr = pred.next;
            curr.lock();
            try {
                while (curr.key < key) {
                    pred.unlock(); // Unlock the predecessor
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (curr.key == key) {
                    pred.next = curr.next;
                    return true; // Item removed
                }
                return false; // Item not found
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    @Override
    public boolean contains(T item) {
        int key = item.hashCode();
        Node pred = null, curr = null;
        head.lock();
        try {
            pred = head;
            curr = pred.next;
            curr.lock();
            try {
                while (curr.key < key) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                return curr.key == key; // Return true if item is found
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }
}
