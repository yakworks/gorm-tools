package org.zalando.problem;

import java.net.URI;

import static org.zalando.problem.Status.BAD_REQUEST;

public final class InsufficientFundsProblem extends ThrowableProblem {

    static final String TYPE_VALUE = "https://example.org/insufficient-funds";
    static final URI TYPE = URI.create(TYPE_VALUE);

    private final int balance;
    private final int debit;

    InsufficientFundsProblem(final int balance, final int debit) {
        this.balance = balance;
        this.debit = debit;
    }

    @Override
    public URI getType() {
        return TYPE;
    }

    @Override
    public String getTitle() {
        return "Insufficient Funds";
    }

    @Override
    public StatusType getStatus() {
        return BAD_REQUEST;
    }

    int getBalance() {
        return balance;
    }

    int getDebit() {
        return debit;
    }

}
