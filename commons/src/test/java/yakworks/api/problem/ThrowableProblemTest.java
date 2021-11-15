package yakworks.api.problem;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static yakworks.api.HttpStatus.BAD_REQUEST;

final class ThrowableProblemTest {

    @Test
    void shouldReturnThrowableProblemCause() {
        final ThrowableProblem problem = Problem.builder()
                .type(URI.create("https://example.org/preauthorization-failed"))
                .title("Preauthorization Failed")
                .status(BAD_REQUEST)
                .cause(ProblemBuilder.create()
                        .type(URI.create("https://example.org/expired-credit-card"))
                        .title("Expired Credit Card")
                        .status(BAD_REQUEST)
                        .build())
                .build();

        assertThat(problem, hasFeature("cause", ThrowableProblem::getCause, notNullValue()));
    }

    @Test
    void shouldReturnNullCause() {
        final ThrowableProblem problem = Problem.builder()
                .type(URI.create("https://example.org/preauthorization-failed"))
                .title("Preauthorization Failed")
                .status(BAD_REQUEST)
                .build();

        assertThat(problem, hasFeature("cause", ThrowableProblem::getCause, nullValue()));
    }

    @Test
    void shouldReturnTitleAsMessage() {
        final ThrowableProblem problem = ProblemBuilder.create()
                .type(URI.create("https://example.org/preauthorization-failed"))
                .title("Preauthorization Failed")
                .status(BAD_REQUEST)
                .build();

        assertThat(problem, hasFeature("message", Throwable::getMessage, is("Preauthorization Failed")));
    }

    @Test
    void shouldReturnTitleAndDetailAsMessage() {
        final ThrowableProblem problem = Problem.builder()
                .type(URI.create("https://example.org/preauthorization-failed"))
                .title("Preauthorization Failed")
                .status(BAD_REQUEST)
                .detail("CVC invalid")
                .build();

        assertThat(problem, hasFeature("message", Throwable::getMessage, is("Preauthorization Failed: CVC invalid")));
    }

    @Test
    void shouldReturnCausesMessage() {
        final ThrowableProblem problem = Problem.builder()
                .type(URI.create("https://example.org/preauthorization-failed"))
                .title("Preauthorization Failed")
                .status(BAD_REQUEST)
                .cause(Problem.builder()
                        .type(URI.create("https://example.org/expired-credit-card"))
                        .title("Expired Credit Card")
                        .status(BAD_REQUEST)
                        .build())
                .build();

        assertThat(problem, hasFeature("cause", Throwable::getCause, is(notNullValue())));
        assertThat(problem.getCause(), hasFeature("message", Throwable::getMessage, is("Expired Credit Card")));
    }

    @Test
    void shouldPrintStackTrace() {
        final ThrowableProblem problem = Problem.builder()
                .type(URI.create("https://example.org/preauthorization-failed"))
                .title("Preauthorization Failed")
                .status(BAD_REQUEST)
                .cause(Problem.builder()
                        .type(URI.create("https://example.org/expired-credit-card"))
                        .title("Expired Credit Card")
                        .status(BAD_REQUEST)
                        .build())
                .build();

        final String stacktrace = getStackTrace(problem);

        assertThat(stacktrace,
                startsWith("{400, Preauthorization Failed"));

        assertThat(stacktrace,
                containsString("Caused by: {400, Expired Credit Card"));
    }

    @Test
    void shouldProcessStackTrace() {
        final ThrowableProblem problem = Problem.builder()
                .type(URI.create("https://example.org/preauthorization-failed"))
                .title("Preauthorization Failed")
                .status(BAD_REQUEST)
                .cause(Problem.builder()
                        .type(URI.create("https://example.org/expired-credit-card"))
                        .title("Expired Credit Card")
                        .status(BAD_REQUEST)
                        .build())
                .build();

        final String stacktrace = getStackTrace(problem);

        // assertThat(stacktrace, not(containsString("org.junit")));
    }

    private String getStackTrace(final Throwable throwable) {
        final StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

}
