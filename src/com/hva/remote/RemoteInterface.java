package com.hva.remote;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    void swapEdges(Integer last, int chunkNumber) throws RemoteException;
    Integer[][] getChunks() throws RemoteException;
    Integer[] bubble(Integer[] arr) throws RemoteException;
    void increaseSortedCounter() throws RemoteException;
    void increaseClientsStarted() throws RemoteException;
    boolean canStartSorting() throws RemoteException;
    void releaseSem(int i) throws RemoteException;
    void acquireSem(int i) throws RemoteException;
    void setChunk(Integer[] chunk, int i) throws RemoteException;
}
