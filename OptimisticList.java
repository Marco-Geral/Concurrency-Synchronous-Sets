import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OptimisticList<T> implements Set<T> {
    private Node head;

    // Node class with lock functionality and volatile 'next'
    private class Node {
        T item;
        int key;
        volatile Node next; // Volatile for consistency with memory model
        Lock lock;

        Node(T item) {
            this.item = item;
            this.key = (item == null) ? Integer.MIN_VALUE : item.hashCode(); // Sentinel for head/tail
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
    public OptimisticList() {
        head = new Node(null); // -∞ sentinel
        head.next = new Node(null); // ∞ sentinel
    }

    // Validation method: ensures that pred is still pointing to curr
    private boolean validate(Node pred, Node curr) {
        Node node = head;
        while (node.key <= pred.key) {
            if (node == pred) {
                return pred.next == curr;
            }
            node = node.next;
        }
        return false;
    }

    @Override
    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            Node pred = head;
            Node curr = pred.next;

            // Traverse the list without acquiring locks
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }

            // Acquire locks on pred and curr
            pred.lock();
            curr.lock();
            try {
                // Validate that pred still points to curr
                if (validate(pred, curr)) {
                    if (curr.key == key) {
                        return false; // Item already exists
                    } else {
                        Node newNode = new Node(item);
                        newNode.next = curr;
                        pred.next = newNode;
                        return true;
                    }
                }
            } finally {
                // Unlock the nodes
                pred.unlock();
                curr.unlock();
            }
        }
    }

    @Override
    public boolean remove(T item) {
        int key = item.hashCode();
        while (true) {
            Node pred = head;
            Node curr = pred.next;

            // Traverse the list without acquiring locks
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }

            // Acquire locks on pred and curr
            pred.lock();
            curr.lock();
            try {
                // Validate that pred still points to curr
                if (validate(pred, curr)) {
                    if (curr.key == key) {
                        pred.next = curr.next;
                        return true; // Item removed
                    } else {
                        return false; // Item not found
                    }
                }
            } finally {
                // Unlock the nodes
                pred.unlock();
                curr.unlock();
            }
        }
    }

    @Override
    public boolean contains(T item) {
        int key = item.hashCode();
        while (true) {
            Node pred = head;
            Node curr = pred.next;

            // Traverse the list without acquiring locks
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }

            // Acquire locks on pred and curr
            pred.lock();
            curr.lock();
            try {
                // Validate that pred still points to curr
                if (validate(pred, curr)) {
                    return curr.key == key; // Return true if item is found
                }
            } finally {
                // Unlock the nodes
                pred.unlock();
                curr.unlock();
            }
        }
    }
}
