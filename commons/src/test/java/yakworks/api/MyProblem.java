package yakworks.api;

import yakworks.api.problem.ThrowableProblem;

import java.net.URI;
import java.util.Map;

@SuppressWarnings("unused") // since we're testing access levels we're fine if this compiles
public final class MyProblem extends ThrowableProblem {

    MyProblem() {}

    MyProblem(final ThrowableProblem cause) {
        super(cause);
    }

}
