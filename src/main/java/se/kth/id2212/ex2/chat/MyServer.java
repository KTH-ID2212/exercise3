package se.kth.id2212.ex2.chat;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class MyServer extends UnicastRemoteObject implements ServerInterface
{
    private List<ClientInterface> clientTable = new ArrayList<>();

    public MyServer() throws RemoteException, MalformedURLException
    {
        super();
        try
        {
            LocateRegistry.getRegistry(1099).list();
        } catch (RemoteException e)

        {
            LocateRegistry.createRegistry(1099);
        }
        Naming.rebind("rmi://localhost/chat", this);
    }

    public List<ClientInterface> getClients()
    {
        return (clientTable);
    }

    public void registerClient(ClientInterface client) throws RemoteException
    {
        if (clientTable.contains(client))
        {
            throw new RemoteException("client already registered");
        }
        clientTable.add(client);
    }

    public void unregisterClient(ClientInterface client) throws RemoteException
    {
        if (!clientTable.contains(client))
        {
            throw new RemoteException("client not registered");
        }
        clientTable.remove(client);
    }

    public void broadcastMsg(String msg) throws RemoteException
    {
        for (ClientInterface client : clientTable)
        {
            try
            {
                client.receiveMsg(msg);
            } catch (RemoteException re)
            {
                re.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        try
        {
            new MyServer();
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
