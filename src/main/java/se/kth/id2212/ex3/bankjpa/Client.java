package se.kth.id2212.ex3.bankjpa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.StringTokenizer;

public class Client
{
    private static final String USAGE = "java bankrmi.Client <bank_url>";
    private static final String DEFAULT_BANK_NAME = "Nordea";
    Account account;
    Bank bankobj;
    private String bankname;
    String clientName;

    static enum CommandName
    {
        newAccount, getAccount, deleteAccount, deposit, withdraw, balance, quit, help;
    }

    ;

    public Client(String bankName)
    {
        this.bankname = bankName;
        try
        {
            bankobj = (Bank) Naming.lookup(bankname);
        } catch (Exception e)
        {
            System.out.println("The runtime failed: " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Connected to bank: " + bankname);
    }

    public Client()
    {
        this(DEFAULT_BANK_NAME);
    }

    public void run()
    {
        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

        while (true)
        {
            System.out.print(clientName + "@" + bankname + ">");
            try
            {
                String userInput = consoleIn.readLine();
                execute(parse(userInput));
            } catch (RejectedException re)
            {
                System.out.println(re);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private Command parse(String userInput)
    {
        if (userInput == null)
        {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(userInput);
        if (tokenizer.countTokens() == 0)
        {
            return null;
        }

        CommandName commandName = null;
        String userName = null;
        float amount = 0;
        int userInputTokenNo = 1;

        while (tokenizer.hasMoreTokens())
        {
            switch (userInputTokenNo)
            {
                case 1:
                    try
                    {
                        String commandNameString = tokenizer.nextToken();
                        commandName = CommandName.valueOf(CommandName.class, commandNameString);
                    } catch (IllegalArgumentException commandDoesNotExist)
                    {
                        System.out.println("Illegal command");
                        return null;
                    }
                    break;
                case 2:
                    userName = tokenizer.nextToken();
                    break;
                case 3:
                    try
                    {
                        amount = Float.parseFloat(tokenizer.nextToken());
                    } catch (NumberFormatException e)
                    {
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

    void execute(Command command) throws RemoteException, RejectedException
    {
        try
        {
            if (command == null)
            {
                return;
            }

            switch (command.getCommandName())
            {
                case quit:
                    System.exit(1);
                case help:
                    for (CommandName commandName : CommandName.values())
                    {
                        System.out.println(commandName);
                    }
                    return;
            }

            String userName = command.getUserName();
            if (userName == null)
            {
                userName = clientName;
                if (userName == null)
                {
                    System.out.println("You must specify account holder");
                    return;
                }
            } else
            {
                clientName = userName;
            }

            switch (command.getCommandName())
            {
                case newAccount:
                    bankobj.newAccount(userName);
                    return;
                case deleteAccount:
                    bankobj.deleteAccount(userName);
                    return;
                case getAccount:
                    Account getAcct = bankobj.findAccount(userName);
                    if (getAcct == null)
                    {
                        System.out.println("No such account.");
                    } else
                    {
                        System.out.println(getAcct);
                    }
                    break;
                case deposit:
                    bankobj.deposit(userName, command.getAmount());
                    break;
                case withdraw:
                    bankobj.withdraw(userName, command.getAmount());
                    break;
                case balance:
                    Account balanceAcct = bankobj.findAccount(userName);
                    if (balanceAcct == null)
                    {
                        System.out.println("No such account.");
                    } else
                    {
                        System.out.println("balance: $" + balanceAcct.getBalance());
                    }
                    break;
                default:
                    System.out.println("Illegal command");
            }
        } catch (Exception e)
        {
            System.out.println("Operation failed, reson: " + e.getMessage());
        }
    }

    private class Command
    {
        private String userName;
        private float amount;
        private CommandName commandName;

        private String getUserName()
        {
            return userName;
        }

        private float getAmount()
        {
            return amount;
        }

        private CommandName getCommandName()
        {
            return commandName;
        }

        private Command(CommandName commandName, String userName, float amount)
        {
            this.commandName = commandName;
            this.userName = userName;
            this.amount = amount;
        }
    }

    public static void main(String[] args)
    {
        if ((args.length > 1) || (args.length > 0 && args[0].equals("-h")))
        {
            System.out.println(USAGE);
            System.exit(1);
        }

        String bankName = null;
        if (args.length > 0)
        {
            bankName = args[0];
            new Client(bankName).run();
        } else
        {
            new Client().run();
        }
    }
}