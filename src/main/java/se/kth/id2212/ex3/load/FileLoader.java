package se.kth.id2212.ex3.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

public class FileLoader {
    private static final String USAGE
            = "USAGE: java bankjdbc.FileLoader filename bankname [datasource] "
              + "[dbms: derby, mysql]";
    private Connection conn;
    private File fileToLoad;
    private String bankName;

    public FileLoader(String fileName, String bankName, String datasource,
                      String dbms) throws ClassNotFoundException, SQLException {
        createDatabase(bankName, datasource, dbms);
        fileToLoad = new File(fileName);
        this.bankName = bankName;
    }

    private void createDatabase(String bankName, String datasource, String dbms)
            throws ClassNotFoundException, SQLException {
        if (dbms.equalsIgnoreCase("derby")) {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            conn = DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/" + datasource + ";create=true");
        } else if (dbms.equalsIgnoreCase("mysql")) {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" + datasource, "root", "javajava");
        } else {
            System.out.println("Unknown dbms");
            System.exit(-1);
        }

        ResultSet result = conn.getMetaData().getTables(null, null, bankName, null);
        if (!result.next()) {
            conn.createStatement().executeUpdate(
                    "CREATE TABLE " + bankName + " (name VARCHAR(32) PRIMARY KEY, balance FLOAT)");
        }
    }

    /**
     * Opens the file and inserts the records from the file into the database.
     *
     * @throws LoadException if loading fails.
     */
    private void loadFile() throws LoadException {
        try {
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + bankName
                                                                + " VALUES (?, ?)");
                 BufferedReader reader = new BufferedReader(new FileReader(fileToLoad))) {
                System.out.println("Loading file <" + fileToLoad + ">\n");

                while (reader.ready()) {
                    String str = reader.readLine();
                    System.out.println(str);
                    StringTokenizer tokenizer = new StringTokenizer(str);
                    String name = tokenizer.nextToken();
                    float balance = 0;
                    if (tokenizer.hasMoreTokens()) {
                        try {
                            balance = Float.parseFloat(tokenizer.nextToken());
                        } catch (NumberFormatException nfe) {
                            System.out.println("Skipping line: " + str);
                            continue;
                        }
                    }
                    stmt.setString(1, name);
                    stmt.setDouble(2, balance);
                    stmt.executeUpdate();
                }
            }
        } catch (IOException | SQLException e) {
            System.out.println(e.getClass().getName() + " caught: " + e.getMessage());
            throw new LoadException("Failed to load", e);
        }
    }

    private void readDb() throws LoadException {
        try {
            try (Statement stmt = conn.createStatement();
                 ResultSet result = stmt.executeQuery("SELECT * FROM " + bankName)) {
                System.out.println("Loading accounts from database: ");
                int row = 1;
                while (result.next()) {
                    System.out.println("Row " + row + ": "
                                       + result.getString("name") + " - $"
                                       + result.getFloat("balance"));
                    row++;
                }
            }
        } catch (SQLException sqle) {
            System.out.println(sqle.getClass().getName() + " caught: "
                               + sqle.getMessage());
            throw new LoadException("Could not read from database.", sqle);
        }
    }

    private void close() throws SQLException {
        conn.close();
    }

    /**
     * The program main method. Loads the specified file, using the specified driver.
     *
     * @param args - The command line argument string: [fileName] [bankName] [datasource] [dbms]
     */
    public static void main(String[] args) {
        String datasource = "Banks";
        String bankName = "NORDEA";
        String dbms = "derby";
        String fileName = "accounts.txt";

        try {
            if (args.length > 0 && args[0].equalsIgnoreCase("-h")) {
                System.out.println(USAGE);
                return;
            }
            if (args.length > 0) {
                fileName = args[0];
            }
            if (args.length > 1) {
                bankName = args[1];
            }
            if (args.length > 2) {
                datasource = args[2];
            }
            if (args.length > 3) {
                dbms = args[3];
            }
            FileLoader app = new FileLoader(fileName, bankName, datasource, dbms);
            app.loadFile();
            app.readDb();
            app.close();
        } catch (ClassNotFoundException | SQLException | LoadException e) {
            e.printStackTrace();
            System.out.println(USAGE);
        }
    }
}
