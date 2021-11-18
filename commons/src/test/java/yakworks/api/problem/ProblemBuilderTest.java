package yakworks.api.problem;

import org.junit.jupiter.api.Test;
import yakworks.api.problem.ProblemException;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static yakworks.api.HttpStatus.BAD_REQUEST;

class ProblemBuilderTest {

    private final URI type = URI.create("https://example.org/out-of-stock");

    @Test
    void shouldCreateEmptyProblem() {
        final Problem problem = Problem.create();

        assertThat(problem, hasFeature("title", Problem::getTitle, is(nullValue())));
        assertThat(problem, hasFeature("detail", Problem::getDetail, is(nullValue())));
        assertThat(problem, hasFeature("instance", Problem::getInstance, is(nullValue())));
    }

    @Test
    void shouldCreateProblem() {
        final Problem problem = ProblemBuilder.create()
                .type(type)
                .title("Out of Stock")
                .status(BAD_REQUEST)
                .build();

        assertThat(problem, hasFeature("type", Problem::getType, is(type)));
        assertThat(problem, hasFeature("title", Problem::getTitle, is("Out of Stock")));
        assertThat(problem, hasFeature("status", Problem::getStatus, is(BAD_REQUEST)));
        assertThat(problem, hasFeature("detail", Problem::getDetail, is(nullValue())));
        assertThat(problem, hasFeature("instance", Problem::getInstance, is(nullValue())));
    }

    @Test
    void shouldCreateProblemWithDetail() {
        final Problem problem = ProblemBuilder.create()
                .type(type)
                .title("Out of Stock")
                .status(BAD_REQUEST)
                .detail("Item B00027Y5QG is no longer available")
                .build();

        assertThat(problem, hasFeature("detail", Problem::getDetail, is("Item B00027Y5QG is no longer available")));
    }

    @Test
    void shouldCreateProblemWithInstance() {
        final Problem problem = ProblemBuilder.create()
                .type(type)
                .title("Out of Stock")
                .status(BAD_REQUEST)
                .instance(URI.create("https://example.com/"))
                .build();

        assertThat(problem, hasFeature("instance", Problem::getInstance, is(URI.create("https://example.com/"))));
    }


    @Test
    void shouldCreateProblemWithCause() {
        final ProblemException problem = ProblemBuilder.create()
                .type(URI.create("https://example.org/preauthorization-failed"))
                .title("Preauthorization Failed")
                .status(BAD_REQUEST)
                .cause(ProblemBuilder.create()
                        .type(URI.create("https://example.org/expired-credit-card"))
                        .title("Expired Credit Card")
                        .status(BAD_REQUEST)
                        .build())
                .build();

        assertThat(problem, hasFeature("cause", ProblemException::getCause, notNullValue()));

        final ProblemException cause = problem.getCause();
        assertThat(cause, hasFeature("type", Problem::getType, hasToString("https://example.org/expired-credit-card")));
        assertThat(cause, hasFeature("title", Problem::getTitle, is("Expired Credit Card")));
        assertThat(cause, hasFeature("status", Problem::getStatus, is(BAD_REQUEST)));
    }


}
