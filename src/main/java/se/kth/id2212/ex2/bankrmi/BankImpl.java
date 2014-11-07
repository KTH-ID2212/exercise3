package se.kth.id2212.ex2.bankrmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class BankImpl extends UnicastRemoteObject implements Bank
{
    private String bankName;
    private Map<String, Account> accounts = new HashMap<>();

    public BankImpl(String bankName) throws RemoteException
    {
        super();
        this.bankName = bankName;
    }

    public synchronized String[] listAccounts()
    {
        return accounts.keySet().toArray(new String[1]);
    }

    public synchronized Account newAccount(String name) throws RemoteException,
            RejectedException
    {
        AccountImpl account = (AccountImpl) accounts.get(name);
        if (account != null)
        {
            System.out.println("Account [" + name + "] exists!!!");
            throw new RejectedException("Rejected: se.kth.id2212.ex2.Bank: " + bankName +
                    " Account for: " + name + " already exists: " + account);
        }
        account = new AccountImpl(name);
        accounts.put(name, account);
        System.out.println("se.kth.id2212.ex2.Bank: " + bankName + " Account: " + account +
                " has been created for " + name);
        return account;
    }

    public synchronized Account getAccount(String name)
    {
        return accounts.get(name);
    }

    public synchronized boolean deleteAccount(String name)
    {
        if (!hasAccount(name))
        {
            return false;
        }
        accounts.remove(name);
        System.out.println("se.kth.id2212.ex2.Bank: " + bankName +
                " Account for " + name + " has been deleted");
        return true;
    }

    private boolean hasAccount(String name)
    {
        if (accounts.get(name) == null)
        {
            return false;
        } else
        {
            return true;
        }
    }
}
