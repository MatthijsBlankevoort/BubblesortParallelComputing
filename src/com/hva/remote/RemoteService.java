package com.hva.remote;

import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Semaphore;

public class RemoteService extends UnicastRemoteObject implements RemoteInterface {
    private static final int PORT = 1199;
    private static final long serialVersionUID = 1L;
    private static final int SEED = 10;
    public static final int SIZE = 3000;
    public static final int THREADS = 3;
    private static Integer[] array = new Integer[SIZE];
    private static Integer[] testArray = new Integer[SIZE];
    private static Integer[][] chunks = new Integer[THREADS][];
    private static Semaphore[] sem = new Semaphore[THREADS];
    private volatile static int sortedCounter = 0;
    private volatile static int clientsStartedCounter = 0;

    private RemoteService() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException {
        initializeArray();
        initializeSemaphores();

        chunks = splitArray(array, SIZE / THREADS);

        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind("//localhost/BubbleSorter", new RemoteService());

        System.out.println("Waiting for all clients...");

        while (clientsStartedCounter != THREADS);

        System.out.println("clients started");

        long startTime = System.currentTimeMillis();

        System.out.println("waiting for sorting...");

        while (sortedCounter != THREADS);

        System.out.println("All sorted");

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(" elapsed time: " + elapsedTime + " milliseconds");

        List<Integer> sortedArray = new ArrayList<Integer>();

        for (Integer[] chunk : chunks) {
            sortedArray.addAll(Arrays.asList(chunk));
        }


        testArray = array;
        Arrays.sort(testArray);
        System.out.println("SORTED ARRAY:");
        for(int i = 0; i < sortedArray.size(); i++) {
            System.out.print(sortedArray.get(i) + " - ");
        }
        assert Arrays.equals(testArray, sortedArray.toArray());
    }

    public void swapEdges(Integer last, int chunkNumber) throws RemoteException {
        Integer first = chunks[chunkNumber + 1][0];

        if (last > first) {
            chunks[chunkNumber][chunks[chunkNumber].length - 1] = first;
            chunks[chunkNumber + 1][0] = last;
        }

    }

    public Integer[][] getChunks() throws RemoteException {
        return chunks;
    }

    public Integer[] bubble(Integer[] arr) throws RemoteException {
        int n = arr.length;

        for (int j = 0; j < n - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
        return arr;
    }

    public void acquireSem(int i) throws RemoteException {
        try {
            sem[i].acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void releaseSem(int i) throws RemoteException {
        sem[i].release();
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

    private static void initializeArray() {
        Random rand = new Random(SEED);
        for (int i = 0; i < SIZE; i++) {
            array[i] = rand.nextInt(SIZE);
        }

        Arrays.sort(array, Collections.reverseOrder());
    }

    private static void initializeSemaphores() {
        for (int i = 0; i < sem.length; i++) {
            sem[i] = new Semaphore(1);
        }
    }


    public void increaseSortedCounter() throws RemoteException {
        synchronized (this) {
            sortedCounter++;
        }
    }

    public boolean canStartSorting() throws RemoteException {
        return clientsStartedCounter == THREADS;
    }

    public Integer[] getChunk(int i) throws RemoteException {
        return chunks[i];
    }

    public void increaseClientsStarted() throws RemoteException {
        System.out.println("CLIENT STARTED");
        synchronized (this) {
            clientsStartedCounter++;
        }
    }

    public void setChunk(Integer[] chunk, int i) throws RemoteException {
//        System.out.println("SETTING CHUNK" + i + " TO ARRAY" + Arrays.toString(chunk));
        chunks[i] = chunk;
    }
}
