package se.kth.id2212.ex2.bankrmi;

final public class RejectedException extends Exception
{
    private static final long serialVersionUID = -314439670131687936L;

    public RejectedException(String reason)
    {
        super(reason);
    }
}
