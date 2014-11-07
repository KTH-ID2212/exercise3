package se.kth.id2212.ex2.chat;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

@SuppressWarnings("serial")
public class MyClient extends UnicastRemoteObject implements ClientInterface
{
    private String id;

    public MyClient(String id) throws RemoteException
    {
        super();
        this.id = id;
    }

    public String getID()
    {
        return id;
    }

    public void receiveMsg(String msg)
    {
        System.out.println(msg);
    }

    public static void main(String args[]) throws RemoteException,
            NotBoundException,
            MalformedURLException
    {
        String myId = args[0];
        MyClient me = new MyClient(myId);

        try
        {
            LocateRegistry.getRegistry(1099).list();
        } catch (RemoteException e)
        {
            LocateRegistry.createRegistry(1099);
        }
        ServerInterface server =
                (ServerInterface) Naming.lookup("rmi://localhost/chat");
        server.registerClient(me);
        server.broadcastMsg(me.getID() + " : Hi guys, I am new ....");

        List<ClientInterface> clients = server.getClients();
        if (!clients.isEmpty())
        {
            System.out.println("\nRegistered Clients ***********");
            for (ClientInterface client : clients)
            {
                System.out.println(client.getID());
            }
            System.out.println("******************************\n");
        }

        for (int i = 0; i < 1000; i++)
        {
            server.broadcastMsg(me.getID() + " : [" + i + "]");
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
        }

        server.broadcastMsg("good bye ...");
        server.unregisterClient(me);
    }
}
