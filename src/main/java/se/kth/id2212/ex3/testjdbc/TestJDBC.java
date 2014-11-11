package se.kth.id2212.ex3.testjdbc;

import java.sql.*;

public class TestJDBC {
    private Connection conn;
    private Statement statement;
    private PreparedStatement insertStatement;
    private PreparedStatement updateStatement;
    private PreparedStatement selectStatement;
    private PreparedStatement deleteStatement;
    private boolean initialized;
    private String dbms = "derby";
    private String datasource = "mydb";

    public static void main(String[] args) {
        TestJDBC tester = new TestJDBC();
        try {
            tester.connect(args);
            tester.createTable();
            tester.selectAll();
            tester.insert("Vlad", 1234);
            tester.insert("Leif", 4321);
            tester.insert("Paris", 4322);
            tester.insert("Hooman", 2323);
            tester.selectAll();
            tester.update("Vlad", 7888);
            tester.selectAll();
//            tester.delete("Hooman");
            tester.selectAll();
//            tester.dropTable();
            tester.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect(String[] args) throws Exception {
        if (args.length > 0) {
            dbms = args[0];
        }
        if (args.length > 1) {
            datasource = args[1];
        }

        if (dbms.equalsIgnoreCase("cloudscape")) {
            Class.forName("COM.cloudscape.core.RmiJdbcDriver");
            conn = DriverManager.getConnection(
                    "jdbc:cloudscape:rmi://localhost:1099/" + datasource + ";create=true;");
        } else if (dbms.equalsIgnoreCase("pointbase")) {
            Class.forName("com.pointbase.jdbc.jdbcUniversalDriver");
            conn = DriverManager.getConnection(
                    "jdbc:pointbase:server://localhost:9092/" + datasource + ",new",
                    "PBPUBLIC",
                    "PBPUBLIC");
        } else if (dbms.equalsIgnoreCase("derby")) {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            conn = DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/" + datasource + ";create=true");
        } else if (dbms.equalsIgnoreCase("mysql")) {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" + datasource, "root", "javajava");
        }

        statement = conn.createStatement();
        System.out.println("Connected to database..." + conn.toString());
    }

    private void close() throws Exception {
        if (initialized) {
            conn.close();
        }
        System.out.println();
        System.out.println("Connection closed...");
    }

    private void createTable() throws Exception {
        ResultSet result = conn.getMetaData().
                getTables(null, null, "ACCOUNT", null);
        if (result.next()) {
            dropTable();
        }

        statement.executeUpdate(
                "CREATE TABLE ACCOUNT (name VARCHAR(32) PRIMARY KEY, balance FLOAT)");
        initialized = true;
        insertStatement = conn.prepareStatement("INSERT INTO ACCOUNT VALUES (?, ?)");
        updateStatement = conn.prepareStatement(
                "UPDATE ACCOUNT SET balance=? WHERE name=?");
        selectStatement = conn.prepareStatement(
                "SELECT * FROM ACCOUNT WHERE name=?");
        deleteStatement = conn.prepareStatement("DELETE FROM ACCOUNT WHERE name=?");
        System.out.println();
        System.out.println("table created...");
    }

    private void insert(String name, float amount) throws Exception {
        insertStatement.setString(1, name);
        insertStatement.setDouble(2, amount);
        int noOfAffectedRows = insertStatement.executeUpdate();
        System.out.println();
        System.out.println("data inserted in " + noOfAffectedRows + " row(s).");
    }

    private void update(String name, float amount) throws Exception {
        float balance = 0;
        selectStatement.setString(1, name);
        ResultSet result = selectStatement.executeQuery();
        if (result.next()) {
            balance = result.getFloat("balance");
        }
        result.close();
        if (amount + balance < 0) {
            throw new Exception("Negative balance is not allowed");
        }
        balance += amount;
        updateStatement.setDouble(1, balance);
        updateStatement.setString(2, name);
        int noOfAffectedRows = updateStatement.executeUpdate();
        System.out.println();
        System.out.println("data updated in " + noOfAffectedRows + " row(s)");
    }

    private void delete(String name) throws Exception {
        deleteStatement.setString(1, name);
        int noOfAffectedRows = deleteStatement.executeUpdate();
        System.out.println();
        System.out.println("data deleted from " + noOfAffectedRows + " row(s)");
    }

    private void selectAll() throws Exception {
        ResultSet result = statement.executeQuery(
                "SELECT * FROM ACCOUNT");
        System.out.println();
        System.out.println("XXXXXXXXXXXXX Selecting data from table XXXXXXXXXXXXXX");
        System.out.println("XXXXXXXX Query returned the following results XXXXXXXX");
        for (int i = 1; result.next(); i++) {
            System.out.println("row " + i + " - " + result.getString("name") + "\t\t\t" + result.
                    getFloat("balance"));
        }
        result.close();
    }

    private void dropTable() throws Exception {
        int NoOfAffectedRows = statement.executeUpdate("DROP TABLE ACCOUNT");
        System.out.println();
        System.out.println("Table dropped, " + NoOfAffectedRows + " row(s) affected");
    }
}
