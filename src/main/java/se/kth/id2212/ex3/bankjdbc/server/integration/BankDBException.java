package se.kth.id2212.ex3.bankjdbc.server.integration;

final public class BankDBException extends Exception {
    private static final long serialVersionUID = -41424240839829672L;

    public BankDBException(String reason) {
        super(reason);
    }

    public BankDBException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}
