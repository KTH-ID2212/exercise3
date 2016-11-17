package se.kth.id2212.ex3.bankjdbc.server.startup;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.sql.SQLException;
import se.kth.id2212.ex3.bankjdbc.server.integration.BankDBException;
import se.kth.id2212.ex3.bankjdbc.server.model.BankImpl;

public class Server {
    private static final int REGISTRY_PORT_NUMBER = 1099;
    private static final String USAGE = "java bankjdbc.Server [rmi-URL of a bank] "
                                        + "[database] [dbms: derby, mysql]";
    private String bankName = "Nordea";
    private String datasource = "Banks";
    private String dbms = "derby";

    public static void main(String[] args) {
        Server server = new Server();
        server.parseCommandLineArgs(args);
        server.startRMIServant();
    }

    private void startRMIServant() {
        try {
            try {
                LocateRegistry.getRegistry(REGISTRY_PORT_NUMBER).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(REGISTRY_PORT_NUMBER);
            }
            BankImpl bankobj = new BankImpl(datasource, dbms);
            java.rmi.Naming.rebind(bankName, bankobj);
            System.out.println(bankobj + " is ready.");
        } catch (RemoteException | MalformedURLException |
                 BankDBException e) {
            System.out.println("Failed to start bank server.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void parseCommandLineArgs(String[] args) {
        if (args.length > 3 || (args.length > 0 && args[0].equalsIgnoreCase("-h"))) {
            System.out.println(USAGE);
            System.exit(1);
        }

        if (args.length > 0) {
            bankName = args[0];
        }

        if (args.length > 1) {
            datasource = args[1];
        }

        if (args.length > 2) {
            dbms = args[2];
        }
    }
}
