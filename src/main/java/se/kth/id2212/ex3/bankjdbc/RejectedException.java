package se.kth.id2212.ex3.bankjdbc;

final public class RejectedException extends Exception {

    private static final long serialVersionUID = -314439670131687936L;

    public RejectedException(String reason) {
        super(reason);
    }

    public RejectedException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}
