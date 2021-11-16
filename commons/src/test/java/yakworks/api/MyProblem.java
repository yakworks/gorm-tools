package yakworks.api;

import yakworks.api.problem.RuntimeProblem;

@SuppressWarnings("unused") // since we're testing access levels we're fine if this compiles
public final class MyProblem extends RuntimeProblem {

    MyProblem() {}

    MyProblem(final RuntimeProblem cause) {
        super(cause);
    }

}
