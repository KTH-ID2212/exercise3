package se.kth.id2212.ex2.bankrmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@SuppressWarnings("serial")
public class AccountImpl extends UnicastRemoteObject implements Account
{
    private float balance = 0;
    private String name;

    /**
     * Constructs a persistently named object.
     */
    public AccountImpl(String name) throws RemoteException
    {
        super();
        this.name = name;
    }

    public synchronized void deposit(float value) throws RemoteException,
            RejectedException
    {
        if (value < 0)
        {
            throw new RejectedException("Rejected: Account " + name +
                    ": Illegal value: " + value);
        }
        balance += value;
        System.out.println("Transaction: Account " + name + ": deposit: $" +
                value + ", balance: $" + balance);
    }

    public synchronized void withdraw(float value) throws RemoteException,
            RejectedException
    {
        if (value < 0)
        {
            throw new RejectedException("Rejected: Account " + name +
                    ": Illegal value: " + value);
        }
        if ((balance - value) < 0)
        {
            throw new RejectedException("Rejected: Account " + name +
                    ": Negative balance on withdraw: " +
                    (balance - value));
        }
        balance -= value;
        System.out.println("Transaction: Account " + name + ": withdraw: $" +
                value + ", balance: $" + balance);
    }

    public synchronized float getBalance() throws RemoteException
    {
        return balance;
    }
}
