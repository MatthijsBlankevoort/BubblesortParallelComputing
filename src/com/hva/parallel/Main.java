package com.hva.parallel;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Main {
    private static final int SEED = 10;
    private static final int SIZE = 20000;
    private static final int CORE = 4;
    private static Integer[] array = new Integer[SIZE];
    private static Integer[] testArray = new Integer[SIZE];
    private static Integer[][] chunks = new Integer[CORE][];
    private static Semaphore[] sem = new Semaphore[CORE];

    static class Worker implements Runnable {

        public Worker() {

        }

        public void run()
        {
            for(int k = 0; k < SIZE / CORE; k++) {
                for(int i = 0; i < chunks.length; i++) {

                    try {
                        sem[i].acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    bubble(chunks[i]);

                    if(i < chunks.length - 1) {
                        try {
                            sem[i+1].acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Integer last = chunks[i][chunks[i].length -1];
                        Integer first = chunks[i+1][0];

                        if(last > first) {
                            chunks[i][chunks[i].length - 1] = first;
                            chunks[i+1][0] = last;
                        }

                        try {
                            sem[i+1].release();
                        } catch (Exception e) {
                            e.printStackTrace();
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
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        initializeArray();
        initializeSemaphores();
        chunks = splitArray(array, SIZE / CORE);

        ExecutorService executor = Executors.newFixedThreadPool(CORE);


        for(int t = 0; t < CORE; t++) {
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


        List<Integer> sortedArray = new ArrayList<Integer>();

        for(int i = 0; i < chunks.length; i++) {
            for(int j = 0; j < chunks[i].length; j++) {
                sortedArray.add(chunks[i][j]);
            }
        }



        testArray = array;
        Arrays.sort(testArray);
        System.out.println("MERGED");
        System.out.println("----------------------------");

        for(int i = 0; i < SIZE; i++) {
            System.out.print(" " + testArray[i]);
        }

        System.out.println("");

        for(int i = 0; i < SIZE; i++) {
            System.out.print(" " + sortedArray.get(i));

        }
        System.out.println("");


        assert Arrays.equals(testArray, sortedArray.toArray());
    }

    private static void bubble(Integer arr[]) {
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

    private static void bubbleSort(Integer arr[]) {
        int n = arr.length;
        for (int i = 0; i < n-1; i++)
            for (int j = 0; j < n-i-1; j++)
                if (arr[j] > arr[j+1])
                {
                    // swap arr[j+1] and arr[i]
                    int temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                }
    }

    private static void initializeSemaphores() {
        for(int i = 0; i < sem.length; i++) {
            sem[i] = new Semaphore(1);
        }
    }

    private static void initializeArray() {
        Random rand = new Random(SEED);
        for (int i = 0; i < SIZE; i++) {
            array[i] = rand.nextInt(SIZE);
        }

        Arrays.sort(array, Collections.reverseOrder());
    }

    private static Integer[][] splitArray(Integer[] arrayToSplit, int chunkSize){
        if(chunkSize<=0){
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
        for(int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++){
            // this copies 'chunk' times 'chunkSize' elements into a new array
            arrays[i] = Arrays.copyOfRange(arrayToSplit, i * chunkSize, i * chunkSize + chunkSize);
        }
        if(rest > 0){ // only when we have a rest
            // we copy the remaining elements into the last chunk
            arrays[chunks - 1] = Arrays.copyOfRange(arrayToSplit, (chunks - 1) * chunkSize, (chunks - 1) * chunkSize + rest);
        }

        return arrays; // that's it
    }
}
