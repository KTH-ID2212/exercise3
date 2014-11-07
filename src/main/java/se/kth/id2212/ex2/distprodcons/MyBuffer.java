package se.kth.id2212.ex2.distprodcons;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class MyBuffer extends UnicastRemoteObject implements RemoteBuffer
{
    LinkedList<Integer> list = new LinkedList<>();

    public MyBuffer() throws RemoteException, MalformedURLException
    {
        super();
        try
        {
            LocateRegistry.getRegistry(1099).list();
        } catch (RemoteException e)
        {
            LocateRegistry.createRegistry(1099);
        }
        Naming.rebind("rmi://localhost/buffer", this);
    }

    public synchronized void put(Integer i) throws RemoteException
    {
        list.addLast(i);
        notifyAll();
    }

    public synchronized Integer get() throws RemoteException
    {
        while (list.size() == 0)
        {
            try
            {
                wait();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        return list.removeFirst();
    }

    public static void main(String[] args)
    {
        try
        {
            new MyBuffer();
        } catch (RemoteException re)
        {
            System.out.println(re);
            System.exit(1);
        } catch (MalformedURLException me)
        {
            System.out.println(me);
            System.exit(1);
        }
    }
}
