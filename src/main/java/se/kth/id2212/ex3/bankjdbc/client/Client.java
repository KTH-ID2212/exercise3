package se.kth.id2212.ex3.bankjdbc.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.StringTokenizer;
import se.kth.id2212.ex3.bankjdbc.server.model.Bank;
import se.kth.id2212.ex3.bankjdbc.server.integration.BankDBException;
import se.kth.id2212.ex3.bankjdbc.server.model.AccountDTO;
import se.kth.id2212.ex3.bankjdbc.server.model.AccountNotUsed;
import se.kth.id2212.ex3.bankjdbc.server.model.RejectedException;

public class Client {

    private static final String USAGE = "java bankrmi.Client <bank_url>";
    private static final String DEFAULT_BANK_NAME = "Nordea";
    AccountDTO account;
    Bank bankobj;
    private String bankname;
    String clientname;

    static enum CommandName {
        newAccount, getAccount, deleteAccount, deposit, withdraw, balance, quit, help, list;
    };

    public Client(String bankName) {
        this.bankname = bankName;
        try {
            bankobj = (Bank) Naming.lookup(bankname);
        } catch (Exception e) {
            System.out.println("The runtime failed: " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Connected to bank: " + bankname);
    }

    public Client() {
        this(DEFAULT_BANK_NAME);
    }

    public void run() {
        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print(clientname + "@" + bankname + ">");
            try {
                String userInput = consoleIn.readLine();
                execute(parse(userInput));
            } catch (RejectedException re) {
                System.out.println(re);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Command parse(String userInput) {
        if (userInput == null) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(userInput);
        if (tokenizer.countTokens() == 0) {
            return null;
        }

        CommandName commandName = null;
        String userName = null;
        int amount = 0;
        int userInputTokenNo = 1;

        while (tokenizer.hasMoreTokens()) {
            switch (userInputTokenNo) {
                case 1:
                    try {
                        String commandNameString = tokenizer.nextToken();
                        commandName = CommandName.valueOf(CommandName.class, commandNameString);
                    } catch (IllegalArgumentException commandDoesNotExist) {
                        System.out.println("Illegal command");
                        return null;
                    }
                    break;
                case 2:
                    userName = tokenizer.nextToken();
                    break;
                case 3:
                    try {
                        amount = Integer.parseInt(tokenizer.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Illegal amount");
                        return null;
                    }
                    break;
                default:
                    System.out.println("Illegal command");
                    return null;
            }
            userInputTokenNo++;
        }
        return new Command(commandName, userName, amount);
    }

    void execute(Command command) throws RemoteException, RejectedException {
        if (command == null) {
            return;
        }

        switch (command.getCommandName()) {
            case list:
                try {
                    for (String accountHolder : bankobj.listAccounts()) {
                        System.out.println(accountHolder);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                return;
            case quit:
                System.exit(0);
            case help:
                for (CommandName commandName : CommandName.values()) {
                    System.out.println(commandName);
                }
                return;
        }

        // all further commands require a name to be specified
        String userName = command.getUserName();
        if (userName == null) {
            userName = clientname;
        }

        if (userName == null) {
            System.out.println("name is not specified");
            return;
        }

        switch (command.getCommandName()) {
            case newAccount:
                clientname = userName;
                bankobj.newAccount(userName);
                return;
        }

        // all further commands require an Account reference
        AccountDTO acct = bankobj.getAccount(userName);
        if (acct == null) {
            System.out.println("No account for " + userName);
            return;
        } else {
            account = acct;
            clientname = userName;
        }

        switch (command.getCommandName()) {
            case deleteAccount:
                clientname = userName;
                bankobj.deleteAccount(acct);
                break;
            case getAccount:
                System.out.println(account);
                break;
            case deposit:
                bankobj.deposit(acct, command.getAmount());
                break;
            case withdraw:
                bankobj.withdraw(acct, command.getAmount());
                break;
            case balance:
                System.out.println("balance: $" + account.getBalance());
                break;
            default:
                System.out.println("Illegal command");
        }
    }

    private class Command {

        private String userName;
        private int amount;
        private CommandName commandName;

        private String getUserName() {
            return userName;
        }

        private int getAmount() {
            return amount;
        }

        private CommandName getCommandName() {
            return commandName;
        }

        private Command(CommandName commandName, String userName, int amount) {
            this.commandName = commandName;
            this.userName = userName;
            this.amount = amount;
        }
    }

    public static void main(String[] args) {
        if ((args.length > 1) || (args.length > 0 && args[0].equals("-h"))) {
            System.out.println(USAGE);
            System.exit(1);
        }

        String bankName = null;
        if (args.length > 0) {
            bankName = args[0];
            new Client(bankName).run();
        } else {
            new Client().run();
        }
    }
}
