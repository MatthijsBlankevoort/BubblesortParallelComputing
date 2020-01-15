package com.hva.remote;
import jdk.swing.interop.SwingInterOpUtils;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RemoteClient {
    private static final int PORT = 1199;
    private static int SIZE = 5000;
    private static int THREADS = 4;

    public static void main(String[] args) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", PORT);
        RemoteInterface service = (RemoteInterface) registry.lookup("//localhost/BubbleSorter");

        for (int k = 0; k < SIZE / THREADS; k++) {
            for (int i = 0; i < THREADS; i++) {
                //TODO: get chunk
                Integer[] chunk = service.getChunk(k);

                //TODO: sort chunk
                service.bubble(chunk, k);

                Integer last = chunk[chunk.length - 1];

                //TODO: swap edges
                service.swapEdges(last, k);
            }
        }
        System.out.println("SORTED");
        service.increaseSortedCounter();
    }
}
