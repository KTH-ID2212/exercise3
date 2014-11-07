package se.kth.id2212.ex2.chat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote
{
    void receiveMsg(String msg) throws RemoteException;

    String getID() throws RemoteException;
}
