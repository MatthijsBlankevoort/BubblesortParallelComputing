package com.hva.remote;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RemoteClient {
    private static final int PORT = 1199;

    public static void main(String[] args) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", PORT);
        RemoteInterface service = (RemoteInterface) registry.lookup("//localhost/BubbleSorter");

        //TODO: get chunk

        //TODO: sort chunk

        //TODO: get next chunk

        //TODO: swap edges
    }
}
