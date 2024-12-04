import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoarseList<T> implements Set<T> {
    private Node head;
    private final Lock lock = new ReentrantLock();

    // Node class representing each element in the list
    private class Node {
        T item;
        int key;
        Node next;

        Node(T item) {
            this.item = item;
            this.key = (item == null) ? Integer.MIN_VALUE : item.hashCode(); // Handle sentinel nodes
        }
    }

    // Constructor initializes the list with sentinel nodes
    public CoarseList() {
        head = new Node(null); // -∞ sentinel
        head.next = new Node(null); // ∞ sentinel
        head.next.key = Integer.MAX_VALUE; // Ensure the sentinel key for the tail is the maximum possible value
    }

    @Override
    public boolean add(T item) {
        Node pred, curr;
        int key = item.hashCode();
        lock.lock();
        try {
            pred = head;
            curr = pred.next;
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            if (key == curr.key) {
                return false; // Item already exists
            } else {
                Node node = new Node(item);
                node.next = curr;
                pred.next = node;
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(T item) {
        Node pred, curr;
        int key = item.hashCode();
        lock.lock();
        try {
            pred = head;
            curr = pred.next;
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            if (key == curr.key) {
                pred.next = curr.next;
                return true; // Item removed
            } else {
                return false; // Item not found
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean contains(T item) {
        Node curr;
        int key = item.hashCode();
        lock.lock();
        try {
            curr = head.next;
            while (curr.key < key) {
                curr = curr.next;
            }
            return key == curr.key; // Return true if item is found
        } finally {
            lock.unlock();
        }
    }
}
