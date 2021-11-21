package yakworks.api;

import yakworks.problem.exception.ProblemException;

@SuppressWarnings("unused") // since we're testing access levels we're fine if this compiles
public final class MyProblem extends ProblemException {

    MyProblem() {}

    MyProblem(final ProblemException cause) {
        super(cause);
    }

}
