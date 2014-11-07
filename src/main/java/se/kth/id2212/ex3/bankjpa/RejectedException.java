package se.kth.id2212.ex3.bankjpa;

public class RejectedException extends Exception
{
    private static final long serialVersionUID = 4601687973395175716L;

    public RejectedException(String reason)
    {
        super(reason);
    }
}