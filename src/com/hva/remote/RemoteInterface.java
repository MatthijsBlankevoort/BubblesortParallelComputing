package com.hva.remote;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    void swapEdges(Integer last, Integer first, Integer chunkNumber) throws RemoteException;
    Integer[] getChunk(Integer chunkNumber) throws RemoteException;
    void bubble(Integer[] arr) throws RemoteException;
}