package com.hva.remote;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class RemoteClient {
    private static final int PORT = 1199;
    private static int SIZE = 3000;
    private static int THREADS = 3;

    public static void main(String[] args) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", PORT);
        RemoteInterface service = (RemoteInterface) registry.lookup("//localhost/BubbleSorter");
        service.increaseClientsStarted();
        Integer[][] chunks = service.getChunks();

        for(int i = 0; i < chunks.length; i++) System.out.println(Arrays.toString(chunks[i]));

        System.out.println("STARTED...WAITING FOR OTHER CLIENT(S)");
        while (!service.canStartSorting());
        System.out.println("STARTING SORTING");
        for (int k = 0; k < SIZE / THREADS; k++) {
            for (int i = 0; i < chunks.length; i++) {
                service.acquireSem(i);
                Integer[] tempArr = service.getChunk(i);

                tempArr = service.bubble(tempArr);

                if (i < THREADS - 1) {
                    service.acquireSem(i + 1);
                    Integer[] tempArr2 = service.getChunk(i+1);
                    Integer last = tempArr[tempArr.length - 1];

                    Integer first = tempArr2[0];

                    if (last > first) {
                        tempArr[tempArr.length - 1] = first;
                        tempArr2[0] = last;
                    }

                    service.setChunk(tempArr2, i+1);
                    service.releaseSem(i + 1);
                }
                service.setChunk(tempArr, i);
                service.releaseSem(i);
            }
        }

        for (Integer[] chunk : service.getChunks()) System.out.println(Arrays.toString(chunk) + " --> " + chunk.length);
        service.increaseSortedCounter();
        System.out.println("SORTED");
    }
}
