package yakworks.api.problem;

import yakworks.api.ApiStatus;

final class GenericProblems {

    GenericProblems() throws Exception {
        throw new IllegalAccessException();
    }

    static ProblemBuilder create(final ApiStatus status) {
        return Problem.builder()
                .title(status.getReason())
                .status(status);
    }

}
