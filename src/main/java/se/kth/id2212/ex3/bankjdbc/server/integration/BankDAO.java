package se.kth.id2212.ex3.bankjdbc.server.integration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import se.kth.id2212.ex3.bankjdbc.server.model.Account;
import se.kth.id2212.ex3.bankjdbc.server.model.AccountDTO;

/**
 * This data access object (DAO) encapsulates all database calls in the bank application. No code
 * outside this class shall have any knowledge about the database.
 */
public class BankDAO {
    private static final String TABLE_NAME = "ACCOUNT";
    private static final String BALANCE_COLUMN_NAME = "BALANCE";
    private static final String NAME_COLUMN_NAME = "NAME";
    private PreparedStatement createAccountStmt;
    private PreparedStatement findAccountStmt;
    private PreparedStatement deleteAccountStmt;
    private PreparedStatement changeBalanceStmt;

    /**
     * Constructs a new DAO object connected to the specified database.
     *
     * @param dbms       Database management system vendor. Currently supported types are "derby"
     *                   and "mysql".
     * @param datasource Database name.
     */
    public BankDAO(String dbms, String datasource) throws BankDBException {
        try {
            Connection connection = createDatasource(dbms, datasource);
            prepareStatements(connection);
        } catch (ClassNotFoundException | SQLException exception) {
            throw new BankDBException("Could not connect to datasource.", exception);
        }
    }

    /**
     * Searches the database for an account whose holder has the specified name.
     *
     * @param holderName The account holder's name
     * @return The account whose holder has the specified name, or <code>null</code> if there is no
     *         such account.
     * @throws BankDBException If failed to search for account.
     */
    public AccountDTO findAccountByName(String holderName) throws BankDBException {
        String failureMsg = "Could not search for specified account.";
        ResultSet result = null;
        try {
            findAccountStmt.setString(1, holderName);
            result = findAccountStmt.executeQuery();
            if (result.next()) {
                return new Account(holderName, result.getInt(BALANCE_COLUMN_NAME), this);
            }
        } catch (SQLException sqle) {
            throw new BankDBException(failureMsg, sqle);
        } finally {
            try {
                result.close();
            } catch (Exception sqle) {
                throw new BankDBException(failureMsg, sqle);
            }
        }
        return null;
    }

    /**
     * Creates a new row in the database, representing the specified account.
     *
     * @param account The account to create.
     * @throws BankDBException If failed to create the specified account.
     */
    public void createAccount(AccountDTO account) throws BankDBException {
        String failureMsg = "Could not create the account: " + account;
        try {
            createAccountStmt.setString(1, account.getHolderName());
            createAccountStmt.setInt(2, account.getBalance());
            int rows = createAccountStmt.executeUpdate();
            if (rows != 1) {
                throw new BankDBException(failureMsg);
            }
        } catch (SQLException sqle) {
            throw new BankDBException(failureMsg, sqle);
        }
    }

    /**
     * Deletes the specified account.
     *
     * @param account The account to delete.
     * @return <code>true</code> if the specified holder had an account and it was deleted,
     *         <code>false</code> if the holder did not have an account and nothing was done.
     * @throws BankDBException If unable to delete the specified account.
     */
    public boolean deleteAccount(AccountDTO account) throws BankDBException {
        try {
            deleteAccountStmt.setString(1, account.getHolderName());
            int rows = deleteAccountStmt.executeUpdate();
            return rows == 1;
        } catch (SQLException sqle) {
            throw new BankDBException("Could not delete the account: " + account, sqle);
        }
    }

    /**
     * All fields in the specified account are stored in the database. The account is identified by
     * the holder name.
     *
     * @param account The account to update.
     * @throws BankDBException If unable to update the specified account.
     */
    public void updateAccount(AccountDTO account) throws BankDBException {
        try {
            changeBalanceStmt.setInt(1, account.getBalance());
            changeBalanceStmt.setString(2, account.getHolderName());
            int rows = changeBalanceStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new BankDBException("Could not update the account: " + account, sqle);
        }
    }

    private Connection createDatasource(String dbms, String datasource) throws
            ClassNotFoundException, SQLException, BankDBException {
        Connection connection = getConnection(dbms, datasource);
        boolean exist = false;
        int tableNameColumn = 3;
        DatabaseMetaData dbm = connection.getMetaData();
        for (ResultSet rs = dbm.getTables(null, null, null, null); rs.next();) {
            if (rs.getString(tableNameColumn).equals(TABLE_NAME)) {
                exist = true;
                rs.close();
                break;
            }
        }
        if (!exist) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE " + TABLE_NAME
                                    + " (" + NAME_COLUMN_NAME + " VARCHAR(32) PRIMARY KEY, "
                                    + BALANCE_COLUMN_NAME + " FLOAT)");
        }
        return connection;
    }

    private Connection getConnection(String dbms, String datasource)
            throws ClassNotFoundException, SQLException, BankDBException {
        if (dbms.equalsIgnoreCase("derby")) {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            return DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/" + datasource + ";create=true");
        } else if (dbms.equalsIgnoreCase("mysql")) {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" + datasource, "root", "javajava");
        } else {
            throw new BankDBException("Unable to create datasource, unknown dbms.");
        }
    }

    private void prepareStatements(Connection connection) throws SQLException {
        createAccountStmt = connection.prepareStatement("INSERT INTO "
                                                        + TABLE_NAME + " VALUES (?, ?)");
        findAccountStmt = connection.prepareStatement("SELECT * from "
                                                      + TABLE_NAME + " WHERE NAME = ?");
        deleteAccountStmt = connection.prepareStatement("DELETE FROM "
                                                        + TABLE_NAME
                                                        + " WHERE name = ?");
        changeBalanceStmt = connection.prepareStatement("UPDATE "
                                                        + TABLE_NAME
                                                        + " SET balance = ? WHERE name= ? ");
    }

}
