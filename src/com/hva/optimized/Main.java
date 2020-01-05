package com.hva.optimized;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final int SEED = 10;
    private static final int SIZE = 5000;
    private static final int CORE = 2;
    private static Integer[] array = new Integer[SIZE];
    private static Integer[] testArray = new Integer[SIZE];
    private static Integer[][] chunks = new Integer[CORE][];
    private static Semaphore[] sem = new Semaphore[CORE];
    private static CyclicBarrier barrier = new CyclicBarrier(CORE,
            new Runnable() {
                public void run() {
                    mergeChunks();
                }
            });
    private static List<Integer> sortedArray = new ArrayList<Integer>();


    static class Worker implements Runnable {
        public Worker() {

        }

        public void run() {
            if (CORE <= 1) {
                for (int k = 0; k < SIZE / CORE; k++) {
                    bubble(chunks[0]);
                }
            } else {
                for (int k = 0; k < SIZE / CORE; k++) {
                    for (int i = 0; i < chunks.length; i++) {

                        try {
                            sem[i].acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        bubble(chunks[i]);

                        if (i < chunks.length - 1) {
                            if(sem[i+1].availablePermits() == 0) {
                                try {
                                    sem[i + 1].acquire();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                Integer last = chunks[i][chunks[i].length - 1];
                                Integer first = chunks[i + 1][0];

                                if (last > first) {
                                    chunks[i][chunks[i].length - 1] = first;
                                    chunks[i + 1][0] = last;
                                }

                                try {
                                    sem[i + 1].release();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Integer last = chunks[i][chunks[i].length - 1];
                                Integer first = chunks[i + 1][0];

                                if (last > first) {
                                    chunks[i][chunks[i].length - 1] = first;
                                    chunks[i + 1][0] = last;
                                }
                            }

                        }

                        try {
                            sem[i].release();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

    }

    private static void bubble(Integer[] arr) {
        int n = arr.length;

        for (int j = 0; j < n - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }

    private static void initializeSemaphores() {
        for (int i = 0; i < sem.length; i++) {
            sem[i] = new Semaphore(1);
        }
    }

    public static void main(String[] args) throws Exception {
        initializeArray();

        initializeSemaphores();

        long startTime = System.currentTimeMillis();

        chunks = splitArray(array, SIZE / CORE);

        ExecutorService executor = Executors.newFixedThreadPool(CORE);

        for (int t = 0; t < CORE; t++) {
            executor.submit(new Worker());
        }
        executor.shutdown();

        try {
            executor.awaitTermination(24L, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(" elapsed time: " + elapsedTime + " milliseconds");

        testArray = array;
        Arrays.sort(testArray);

//        for (Integer[] chunk : chunks) {
//            sortedArray.addAll(Arrays.asList(chunk));
//        }

//        System.out.println("MERGED");
//        System.out.println("----------------------------");

        assert Arrays.equals(testArray, sortedArray.toArray());
    }

    private static void initializeArray() {
        Random rand = new Random(SEED);
        for (int i = 0; i < SIZE; i++) {
            array[i] = rand.nextInt(SIZE);
        }

        Arrays.sort(array, Collections.reverseOrder());
    }

    private static void mergeChunks() {
        for (Integer[] chunk : chunks) {
            //                System.out.println("chunk get " + chunk.get(i));
            sortedArray.addAll(Arrays.asList(chunk));
        }

    }

    private static Integer[][] splitArray(Integer[] arrayToSplit, int chunkSize) {
        if (chunkSize <= 0) {
            return null;  // just in case :)
        }
        // first we have to check if the array can be split in multiple
        // arrays of equal 'chunk' size
        int rest = arrayToSplit.length % chunkSize;  // if rest>0 then our last array will have less elements than the others
        // then we check in how many arrays we can split our input array
        int chunks = arrayToSplit.length / chunkSize + (rest > 0 ? 1 : 0); // we may have to add an additional array for the 'rest'
        // now we know how many arrays we need and create our result array
        Integer[][] arrays = new Integer[chunks][];
        // we create our resulting arrays by copying the corresponding
        // part from the input array. If we have a rest (rest>0), then
        // the last array will have less elements than the others. This
        // needs to be handled separately, so we iterate 1 times less.
        for (int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++) {
            // this copies 'chunk' times 'chunkSize' elements into a new array
            arrays[i] = Arrays.copyOfRange(arrayToSplit, i * chunkSize, i * chunkSize + chunkSize);
        }
        if (rest > 0) { // only when we have a rest
            // we copy the remaining elements into the last chunk
            arrays[chunks - 1] = Arrays.copyOfRange(arrayToSplit, (chunks - 1) * chunkSize, (chunks - 1) * chunkSize + rest);
        }

        return arrays; // that's it
    }
}
