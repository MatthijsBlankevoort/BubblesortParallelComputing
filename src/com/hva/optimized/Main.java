package com.hva.optimized;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Main {
    private static final int SEED = 10;
    private static final int SIZE = 80000;
    private static final int CORE = 4;
    private static AtomicIntegerArray array;
    private static int[] ints = new int[SIZE];
    private static Integer[] integers = new Integer[SIZE];
    private static Integer[] testArray = new Integer[SIZE];
    private static AtomicIntegerArray[] chunks = new AtomicIntegerArray[CORE];
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
                for (int  k = 0; k < SIZE / CORE; k++) {
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

                                int last = chunks[i].get(chunks[i].length() - 1);
                                int first = chunks[i + 1].get(0);

                                if (last > first) {
                                    chunks[i].set(chunks[i].length() - 1, first);
                                    chunks[i + 1].set(0, last);
                                }

                                try {
                                    sem[i + 1].release();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                int last = chunks[i].get(chunks[i].length() - 1);
                                int first = chunks[i + 1].get(0);

                                if (last > first) {
                                    chunks[i].set(chunks[i].length() - 1, first);
                                    chunks[i + 1].set(0, last);
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

    private static void bubble(AtomicIntegerArray arr) {
        int n = arr.length();

        for (int j = 0; j < n - 1; j++) {
            if (arr.get(j) > arr.get(j + 1)) {
                int temp = arr.get(j);
                arr.set(j, arr.get(j + 1));
                arr.set(j + 1, temp);
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

        testArray = integers;
        Arrays.sort(testArray);

//        System.out.println("MERGED");
//        System.out.println("----------------------------");

//        System.out.println("test array " + Arrays.toString(testArray));

//        System.out.println("sorted array" + Arrays.toString(sortedArray.toArray()));

        assert Arrays.equals(testArray, sortedArray.toArray());
    }

    private static void initializeArray() {
        Random rand = new Random(SEED);
        for (int i = 0; i < SIZE; i++) {
            integers[i] = rand.nextInt(SIZE);
        }
        Arrays.sort(integers, Collections.reverseOrder());

        for (int i = 0; i < SIZE; i++) {
            ints[i] = integers[i];
        }

        array = new AtomicIntegerArray(ints);
    }

    public static void mergeChunks() {
        for (AtomicIntegerArray chunk : chunks) {
            for (int i = 0; i < chunk.length(); i++) {
//                System.out.println("chunk get " + chunk.get(i));
                sortedArray.add(chunk.get(i));
            }
        }

    }

    private static AtomicIntegerArray createAtomicArrayChunk(int start, int end, int chunkSize) {
        AtomicIntegerArray atomicIntegerArrayChunk = new AtomicIntegerArray(chunkSize);
        for (int i = start, j = 0; i < end; i++, j++) {
            atomicIntegerArrayChunk.set(j, array.get(i));
        }
        return atomicIntegerArrayChunk;
    }

    private static AtomicIntegerArray[] splitArray(AtomicIntegerArray arrayToSplit, int chunkSize) {
        if (chunkSize <= 0) {
            return null;
        }
        int rest = arrayToSplit.length() % chunkSize;
        int chunks = arrayToSplit.length() / chunkSize + (rest > 0 ? 1 : 0);
        AtomicIntegerArray[] arrays = new AtomicIntegerArray[chunks];
        for (int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++) {
            arrays[i] = createAtomicArrayChunk(i * chunkSize, i * chunkSize + chunkSize, chunkSize);
        }
        if (rest > 0) {
            arrays[chunks - 1] = createAtomicArrayChunk((chunks - 1) * chunkSize, (chunks - 1) * chunkSize + rest, chunkSize);
        }

        return arrays;
    }
}
