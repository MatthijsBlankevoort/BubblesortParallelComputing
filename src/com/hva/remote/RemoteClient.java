package com.hva.remote;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class RemoteClient {
    private static final int PORT = 1199;
    private static int SIZE = 9;
    private static int THREADS = 3;

    public static void main(String[] args) throws RemoteException, NotBoundException {
        System.out.println("doing something");
        Registry registry = LocateRegistry.getRegistry("169.254.1.1", PORT);
        RemoteInterface service = (RemoteInterface) registry.lookup("//169.254.1.1/BubbleSorter");
        service.increaseClientsStarted();

        while(!service.canStartSorting()) {
            System.out.println("PIK IK WACHT OPJE");
        }

            for (int k = 0; k < SIZE / THREADS; k++) {
            for (int i = 0; i < THREADS; i++) {
                //TODO: get chunk
                service.acquireSem(i);
                Integer[] chunk = service.getChunk(i);

                //TODO: sort chunk
                service.bubble(chunk);
                service.setChunk(chunk, i);
                Integer last = chunk[chunk.length - 1];

                if(i < THREADS - 1) {
                    service.acquireSem(i+1);

                    //TODO: swap edges
                    service.swapEdges(last, i);
                    service.releaseSem(i+1);
                }

                service.releaseSem(i);
            }
        }
        System.out.println("SORTED");
        service.increaseSortedCounter();
    }
}
