package yakworks.api.problem;

final class GenericProblems {

    GenericProblems() throws Exception {
        throw new IllegalAccessException();
    }

    static ProblemBuilder create(final StatusType status) {
        return Problem.builder()
                .title(status.getReasonPhrase())
                .status(status);
    }

}
