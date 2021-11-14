package org.zalando.problem;

import java.net.URI;

import static org.zalando.problem.Status.BAD_REQUEST;

public final class OutOfStockProblem extends ThrowableProblem {

    private static final String TYPE_VALUE = "https://example.org/out-of-stock";
    private static final URI TYPE = URI.create(TYPE_VALUE);

    private final String detail;

    public OutOfStockProblem(final String detail) {
        this.detail = detail;
    }

    @Override
    public URI getType() {
        return TYPE;
    }

    @Override
    public String getTitle() {
        return "Out of Stock";
    }

    @Override
    public StatusType getStatus() {
        return BAD_REQUEST;
    }

    @Override
    public String getDetail() {
        return detail;
    }

}
