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
        for (Integer[] chunk : chunks) System.out.println("client chunks " + Arrays.toString(chunk));

        while (!service.canStartSorting());

        for (int k = 0; k < SIZE; k++) {
            for (int i = 0; i < chunks.length; i++) {
                service.acquireSem(i);

                chunks[i] = service.bubble(chunks[i]);

                if (i < THREADS - 1) {
                    service.acquireSem(i + 1);

                    //TODO: swap edges
                    Integer last = chunks[i][chunks[i].length - 1];
                    Integer first = chunks[i + 1][0];

                    if (last > first) {
                        chunks[i][chunks[i].length - 1] = first;
                        chunks[i + 1][0] = last;
                    }
                    service.setChunk(chunks[i+1], i+1);

                    service.releaseSem(i + 1);
                }
                service.setChunk(chunks[i], i);
                service.releaseSem(i);
            }
        }
        for (Integer[] chunk : chunks) System.out.println(Arrays.toString(chunk));
        service.increaseSortedCounter();
        System.out.println("SORTED");
    }
}
