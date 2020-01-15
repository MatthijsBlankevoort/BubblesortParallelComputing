package com.hva.remote;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    void swapEdges(Integer last, int chunkNumber) throws RemoteException;
    Integer[] getChunk(Integer chunkNumber) throws RemoteException;
    void bubble(Integer[] arr) throws RemoteException;
    void increaseSortedCounter() throws RemoteException;
    void releaseSem(int i) throws RemoteException;
    void acquireSem(int i) throws RemoteException;
}
