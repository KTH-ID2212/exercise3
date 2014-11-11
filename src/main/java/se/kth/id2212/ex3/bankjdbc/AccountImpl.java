package se.kth.id2212.ex3.bankjdbc;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class AccountImpl extends UnicastRemoteObject implements Account {

    private float balance;
    private String name;
    private PreparedStatement updateStatement;

    public AccountImpl(String name, float balance, Connection connection)
            throws RemoteException, RejectedException {
        super();
        this.name = name;
        this.balance = balance;
        try {
            updateStatement = connection.prepareStatement("UPDATE "
                                                                  + BankImpl.TABLE_NAME
                                                          + " SET balance = ? WHERE name= ? ");
            updateStatement.setString(2, name);
        } catch (SQLException sqle) {
            throw new RejectedException("Unable to instantiate account", sqle);
        }
    }

    public AccountImpl(String name, Connection connection)
            throws RemoteException, RejectedException {
        this(name, 0, connection);
    }

    @Override
    public synchronized void deposit(float value) throws RemoteException,
                                                         RejectedException {
        if (value < 0) {
            throw new RejectedException("Rejected: Account " + name
                                                + ": Illegal value: " + value);
        }

        boolean success = false;
        try {
            balance += value;
            updateStatement.setDouble(1, balance);
            int rows = updateStatement.executeUpdate();
            if (rows != 1) {
                throw new RejectedException("Unable to deposit into account: " + name);
            } else {
                success = true;
            }
            System.out.println("Transaction: Account " + name + ": deposit: $"
                                       + value + ", balance: $" + balance);
        } catch (SQLException sqle) {
            throw new RejectedException("Unable to deposit into account: " + name, sqle);
        } finally {
            if (!success) {
                balance -= value;
            }
        }
    }

    @Override
    public synchronized void withdraw(float value) throws RemoteException,
                                                          RejectedException {
        if (value < 0) {
            throw new RejectedException("Rejected: Account " + name
                                                + ": Illegal value: " + value);
        }

        if ((balance - value) < 0) {
            throw new RejectedException("Rejected: Account " + name
                                                + ": Negative balance on withdraw: "
                                                + (balance - value));
        }

        boolean success = false;
        try {
            balance -= value;
            updateStatement.setDouble(1, balance);
            int rows = updateStatement.executeUpdate();
            if (rows != 1) {
                throw new RejectedException("Unable to deposit into account: " + name);
            } else {
                success = true;
            }
            System.out.println("Transaction: Account " + name + ": deposit: $"
                                       + value + ", balance: $" + balance);
        } catch (SQLException sqle) {
            throw new RejectedException("Unable to deposit into account: " + name, sqle);
        } finally {
            if (!success) {
                balance += value;
            }
        }
    }

    @Override
    public synchronized float getBalance() throws RemoteException {
        return balance;
    }
}
