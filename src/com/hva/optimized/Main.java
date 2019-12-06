package com.hva.optimized;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Main {
    private static final int SEED = 10;
    private static final int SIZE = 20;
    private static final int CORE = 4;
    private static AtomicIntegerArray array;
    private static int[] ints = new int[SIZE];
    private static Integer[] integers = new Integer[SIZE];
    private static Integer[] testArray = new Integer[SIZE];
    private static AtomicIntegerArray[] chunks = new AtomicIntegerArray[CORE];
    private static Semaphore[] sem = new Semaphore[CORE];

//    static class Worker implements Runnable {
//
//        public Worker() {
//
//        }
//
//        public void run()
//        {
//            for(int k = 0; k < SIZE / CORE; k++) {
//                for(int i = 0; i < chunks.length; i++) {
//
//                    try {
//                        sem[i].acquire();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//
//                    bubble(chunks[i]);
//
//                    if(i < chunks.length - 1) {
//                        try {
//                            sem[i+1].acquire();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                        Integer last = chunks[i][chunks[i].length -1];
//                        Integer first = chunks[i+1][0];
//
//                        if(last > first) {
//                            chunks[i][chunks[i].length - 1] = first;
//                            chunks[i+1][0] = last;
//                        }
//
//                        try {
//                            sem[i+1].release();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    try {
//                        sem[i].release();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        }
//    }

    private static void bubble(Integer[] arr) {
        int n = arr.length;

        for (int j = 0; j < n-1; j++) {
            if (arr[j] > arr[j+1])
            {
                int temp = arr[j];
                arr[j] = arr[j+1];
                arr[j+1] = temp;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        initializeArray();

        long startTime = System.currentTimeMillis();

        chunks = splitArray(array, SIZE / CORE);

//        for(int i = 0; i < CORE; i++) {
//            for (int j = 0; j < SIZE; j++) {
//                assert chunks != null;
//                System.out.println(chunks[i].get(j));
//            }
//        }

//        ExecutorService executor = Executors.newFixedThreadPool(CORE);
//
//        for(int t = 0; t < CORE; t++) {
//            executor.submit(new Worker());
//        }
//
//        executor.shutdown();
//
//        try {
//            executor.awaitTermination(24L, TimeUnit.HOURS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        long stopTime = System.currentTimeMillis();
//        long elapsedTime = stopTime - startTime;
//        System.out.println(" elapsed time: " + elapsedTime + " milliseconds");
//
//
//        List<Integer> sortedArray = new ArrayList<Integer>();
//
//        for (Integer[] chunk : chunks) {
//            sortedArray.addAll(Arrays.asList(chunk));
//        }
//
//        testArray = integers;
//        Arrays.sort(testArray);
//        System.out.println("MERGED");
//        System.out.println("----------------------------");
//
//        for(int i = 0; i < SIZE; i++) {
//            System.out.print(" " + testArray[i]);
//        }
//
//        System.out.println("");
//
//        for(int i = 0; i < SIZE; i++) {
//            System.out.print(" " + sortedArray.get(i));
//
//        }
//        System.out.println("");
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

    private static AtomicIntegerArray createAtomicArrayChunk(int start, int end, int chunkSize) {
        AtomicIntegerArray atomicIntegerArrayChunk = new AtomicIntegerArray(chunkSize);
        for(int i = start; i < end; i++) {
            atomicIntegerArrayChunk.set(i, array.get(i));
        }
        return atomicIntegerArrayChunk;
    }

    private static AtomicIntegerArray[] splitArray(AtomicIntegerArray arrayToSplit, int chunkSize){
        if(chunkSize<=0){
            return null;
        }
        int rest = arrayToSplit.length() % chunkSize;
        int chunks = arrayToSplit.length() / chunkSize + (rest > 0 ? 1 : 0);
        AtomicIntegerArray[] arrays = new AtomicIntegerArray[chunks];
        for(int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++){
            arrays[i] = createAtomicArrayChunk(i * chunkSize, i * chunkSize + chunkSize, chunkSize);
        }
        if(rest > 0){
            arrays[chunks - 1] = createAtomicArrayChunk((chunks - 1) * chunkSize, (chunks - 1) * chunkSize + rest, chunkSize);
        }
        return arrays;
    }
}
