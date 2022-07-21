package yakworks.problem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import yakworks.problem.exception.ProblemBuilder;
import yakworks.problem.exception.ProblemRuntime;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static yakworks.api.HttpStatus.BAD_REQUEST;

@SuppressWarnings("unchecked")
class ProblemBuilderTest {

    private final URI type = URI.create("https://example.org/out-of-stock");

    @Test
    void shouldCreateEmptyProblem() {
        final IProblem problem = CreateProblem.create();

        assertThat(problem, hasFeature("title", IProblem::getTitle, is(nullValue())));
        assertThat(problem, hasFeature("detail", IProblem::getDetail, is(nullValue())));
    }

    @Test
    void shouldCreateProblem() {
        final IProblem problem = ProblemBuilder.of(ProblemRuntime.class)
                .type(type)
                .title("Out of Stock")
                .status(BAD_REQUEST)
                .build();

        assertThat(problem, hasFeature("type", IProblem::getType, is(type)));
        assertThat(problem, hasFeature("title", IProblem::getTitle, is("Out of Stock")));
        assertThat(problem, hasFeature("status", IProblem::getStatus, is(BAD_REQUEST)));
        assertThat(problem, hasFeature("detail", IProblem::getDetail, is(nullValue())));
    }

    @Test
    void shouldCreateProblemWithDetail() {
        final IProblem problem = new ProblemBuilder()
                .type(type)
                .title("Out of Stock")
                .status(BAD_REQUEST)
                .detail("Item B00027Y5QG is no longer available")
                .build();

        assertThat(problem, hasFeature("detail", IProblem::getDetail, is("Item B00027Y5QG is no longer available")));
    }

    @Test @Disabled
    void shouldCreateProblemWithCause() {
        final ProblemRuntime problem = (ProblemRuntime) new ProblemBuilder(ProblemRuntime.class)
                .type(URI.create("https://example.org/preauthorization-failed"))
                .title("Preauthorization Failed")
                .status(BAD_REQUEST)
                .cause((Throwable) new ProblemBuilder(ProblemRuntime.class)
                        .type(URI.create("https://example.org/expired-credit-card"))
                        .title("Expired Credit Card")
                        .status(BAD_REQUEST)
                        .build())
                .build();

        // assertThat(problem, hasFeature("cause", ProblemRuntime::getCause, notNullValue()));

        final ProblemRuntime cause = (ProblemRuntime)problem.getCause();
        assertThat(cause, hasFeature("type", IProblem::getType, hasToString("https://example.org/expired-credit-card")));
        assertThat(cause, hasFeature("title", IProblem::getTitle, is("Expired Credit Card")));
        assertThat(cause, hasFeature("status", IProblem::getStatus, is(BAD_REQUEST)));
    }


}
