package yakworks.api;

import yakworks.api.problem.AbstractThrowableProblem;
import yakworks.api.problem.StatusType;
import yakworks.api.problem.ThrowableProblem;

import java.net.URI;
import java.util.Map;

@SuppressWarnings("unused") // since we're testing access levels we're fine if this compiles
public final class MyProblem extends AbstractThrowableProblem {

    MyProblem() {

    }

    MyProblem(final URI type) {
        super(type);
    }

    MyProblem(final URI type, final String title) {
        super(type, title);
    }

    MyProblem(final URI type, final String title,
            final StatusType status) {
        super(type, title, status);
    }

    MyProblem(final URI type, final String title,
            final StatusType status, final String detail) {
        super(type, title, status, detail);
    }

    MyProblem(final URI type, final String title,
            final StatusType status, final String detail,
            final URI instance) {
        super(type, title, status, detail, instance);
    }

    MyProblem( final URI type,  final String title,
             final StatusType status,  final String detail,
             final URI instance,  final ThrowableProblem cause) {
        super(type, title, status, detail, instance, cause);
    }

    MyProblem( final URI type,  final String title,
             final StatusType status,  final String detail,
             final URI instance,  final ThrowableProblem cause,
             final Map<String, Object> parameters) {
        super(type, title, status, detail, instance, cause, parameters);
    }

}
