package se.kth.id2212.ex3.bankjpa;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Server
{
    static final String USAGE = "java bankrmi.Server [rmi-URL of a bank]";
    static final String BANK = "Nordea";

    public static void main(String[] args) throws Exception
    {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        String bankname = (args.length > 0) ? args[0] : BANK;
        if (bankname.equalsIgnoreCase("-h"))
        {
            System.out.println(USAGE);
            System.exit(1);
        }
        try
        {
            Bank bankobj = new BankImpl();
            try
            {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e)
            {
                LocateRegistry.createRegistry(1099);
            }
            Naming.rebind(bankname, bankobj);
            System.out.println(bankobj + " is ready.");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
