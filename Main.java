import java.util.concurrent.*;
import java.util.Random;

public class Main {
    // Number of threads to simulate
    private static final int NUM_THREADS = 10;
    private static final int NUM_OPERATIONS = 1000;

    public static void main(String[] args) throws InterruptedException {
        // Create instances of each synchronization type
        Set<Integer> coarseGrainedSet = new CoarseList<>();
        Set<Integer> fineGrainedSet = new FineGrainedList<>();
        Set<Integer> optimisticSet = new OptimisticList<>();

        System.out.println("Testing Coarse-Grained Synchronization:");
        runTest(coarseGrainedSet);

        System.out.println("\nTesting Fine-Grained Synchronization:");
        runTest(fineGrainedSet);

        System.out.println("\nTesting Optimistic Synchronization:");
        runTest(optimisticSet);
    }

    // Method to run scenarios and measure time taken
    private static void runTest(Set<Integer> set) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        long startTime, endTime;

        // Scenario 1: Low contention, many threads
        System.out.println("Scenario 1: Low Contention, Many Threads");
        startTime = System.currentTimeMillis();
        for (int i = 0; i < NUM_THREADS; i++) {
            executor.submit(() -> performAddAndContainsOperations(set, 0, NUM_OPERATIONS));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + " ms");

        // Scenario 2: High contention, many threads
        executor = Executors.newFixedThreadPool(NUM_THREADS);
        System.out.println("Scenario 2: High Contention, Many Threads");
        startTime = System.currentTimeMillis();
        for (int i = 0; i < NUM_THREADS; i++) {
            executor.submit(() -> performAddAndRemoveOperations(set, 0, 10)); // Higher contention on same keys
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + " ms");

        // Scenario 3: Mixed operations, varying contention
        executor = Executors.newFixedThreadPool(NUM_THREADS);
        System.out.println("Scenario 3: Mixed Operations");
        startTime = System.currentTimeMillis();
        for (int i = 0; i < NUM_THREADS; i++) {
            executor.submit(() -> performMixedOperations(set, NUM_OPERATIONS));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
    }

    // Perform add and contains operations for low contention scenario
    private static void performAddAndContainsOperations(Set<Integer> set, int startRange, int numOps) {
        Random rand = new Random();
        for (int i = 0; i < numOps; i++) {
            int value = startRange + rand.nextInt(10000); // Large range to minimize contention
            set.add(value);
            set.contains(value);
        }
    }

    // Perform add and remove operations for high contention scenario
    private static void performAddAndRemoveOperations(Set<Integer> set, int startRange, int numOps) {
        Random rand = new Random();
        for (int i = 0; i < numOps; i++) {
            int value = startRange + rand.nextInt(10); // Small range to increase contention
            set.add(value);
            set.remove(value);
        }
    }

    // Perform mixed operations (add, remove, contains) for mixed operation scenario
    private static void performMixedOperations(Set<Integer> set, int numOps) {
        Random rand = new Random();
        for (int i = 0; i < numOps; i++) {
            int value = rand.nextInt(100); // Moderate range for mixed contention
            switch (rand.nextInt(3)) {
                case 0:
                    set.add(value);
                    break;
                case 1:
                    set.remove(value);
                    break;
                case 2:
                    set.contains(value);
                    break;
            }
        }
    }
}
