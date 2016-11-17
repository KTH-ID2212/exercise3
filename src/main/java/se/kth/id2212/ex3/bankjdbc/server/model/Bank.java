package se.kth.id2212.ex3.bankjdbc.server.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Specified the bank's remote methods.
 */
public interface Bank extends Remote {
    /**
     * Creates an account with the specified name and the balance zero.
     *
     * @param name The account holder's name.
     * @throws RemoteException   If unable to complete the RMI call.
     * @throws RejectedException If unable to create the account.
     */
    public void newAccount(String name) throws RemoteException, RejectedException;

    /**
     * Returns the account of the specified holder, or <code>null</code> if there is no such
     * account.
     *
     * @param holderName The holder whose account to search for.
     * @return The account of the specified holder, or <code>null</code> if there is no such
     *         account.
     * @throws RemoteException   If unable to complete the RMI call.
     * @throws RejectedException If unable to search for the account.
     */
    public AccountDTO getAccount(String name) throws RemoteException, RejectedException;

    /**
     * Deletes the account with the specified holder, if there is such an account. If there is no
     * such account, nothing happens.
     *
     * @param account The account to delete.
     * @return <code>true</code> if the specified holder had an account and it was deleted,
     *         <code>false</code> if the holder did not have an account and nothing was done.
     * @throws RemoteException   If unable to complete the RMI call.
     * @throws RejectedException If unable to delete account, or unable to check if there was an
     *                           account to delete.
     */
    public boolean deleteAccount(AccountDTO account) throws RemoteException, RejectedException;

    /**
     * @return All accounts that have been used since the server started.
     * @throws RemoteException If unable to complete the RMI call.
     */
    public String[] listAccounts() throws RemoteException;

    /**
     * Deposits the specified amount to the specified account.
     *
     * @param acct   The account to which to deposit.
     * @param amount The amount to deposit.
     * @throws RemoteException   If unable to complete the RMI call.
     * @throws RejectedException If the specified amount is negative, or if unable to perform the
     *                           update.
     */
    public void deposit(AccountDTO acct, int amt) throws RemoteException, RejectedException;

    /**
     * Withdraws the specified amount from the specified account.
     *
     * @param acct   The account from which to withdraw.
     * @param amount The amount to withdraw.
     * @throws RemoteException   If unable to complete the RMI call.
     * @throws RejectedException If the specified amount is negative, if the amount is larger than
     *                           the balance, or if unable to perform the update.
     */
    public void withdraw(AccountDTO acct, int amt) throws RemoteException, RejectedException;
}
