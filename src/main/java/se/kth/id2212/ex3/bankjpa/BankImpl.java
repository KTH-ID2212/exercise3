package se.kth.id2212.ex3.bankjpa;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

@SuppressWarnings("serial")
public class BankImpl extends UnicastRemoteObject implements Bank
{
    private EntityManagerFactory emFactory;

    public BankImpl() throws RemoteException
    {
        super();
        emFactory = Persistence.createEntityManagerFactory("bank");
    }

    public Account newAccount(String name) throws RejectedException
    {
        EntityManager em = null;
        try
        {
            em = beginTransaction();
            List<Account> existingAccounts = em.createNamedQuery("findAccountWithName", Account.class).
                    setParameter("ownerName", name).getResultList();
            if (existingAccounts.size() != 0)
            {
                // account exists, can not be created.
                throw new RejectedException("Rejected: Account for: " + name + " already exists");
            }

            // create account.
            Account account = new Account(new Owner(name), 0);
            em.persist(account);
            return account;
        } finally
        {
            commitTransaction(em);
        }
    }

    public void deposit(String ownerName, float value) throws RejectedException
    {
        EntityManager em = null;
        try
        {
            em = beginTransaction();

            getAccount(ownerName, em).deposit(value);
        } finally
        {
            commitTransaction(em);
        }
    }

    public void withdraw(String ownerName, float value) throws RejectedException
    {
        EntityManager em = null;
        try
        {
            em = beginTransaction();

            getAccount(ownerName, em).withdraw(value);
        } finally
        {
            commitTransaction(em);
        }
    }

    public Account findAccount(String ownerName)
    {
        EntityManager em = null;
        try
        {
            em = beginTransaction();

            Account account = getAccount(ownerName, em);
            return account;
        } finally
        {
            commitTransaction(em);
        }
    }

    public void deleteAccount(String name)
    {
        EntityManager em = null;
        try
        {
            em = beginTransaction();

            Account account = getAccount(name, em);
            em.remove(account);

        } finally
        {
            commitTransaction(em);
        }
    }

    private Account getAccount(String ownerName, EntityManager em)
    {
        if (ownerName == null)
        {
            return null;
        }

        try
        {
            return (Account) em.createNamedQuery("findAccountWithName").
                    setParameter("ownerName", ownerName).getSingleResult();
        } catch (NoResultException noSuchAccount)
        {
            return null;
        }
    }

    private EntityManager beginTransaction()
    {
        EntityManager em = emFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        return em;
    }

    private void commitTransaction(EntityManager em)
    {
        em.getTransaction().commit();
    }
}
