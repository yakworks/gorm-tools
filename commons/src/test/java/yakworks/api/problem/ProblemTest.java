package yakworks.api.problem;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static yakworks.api.HttpStatus.NOT_FOUND;

final class ProblemTest {

    @Test
    void shouldUseDefaultType() {
        final Problem problem = new EmptyProblem();

        assertThat(problem, hasFeature("type", Problem::getType, hasToString("about:blank")));
    }

    @Test
    void shouldUseDefaultTitle() {
        final Problem problem = new EmptyProblem();

        assertThat(problem, hasFeature("title", Problem::getTitle, is(nullValue())));
    }

    @Test
    void shouldUseDefaultDetail() {
        final Problem problem = new EmptyProblem();

        assertThat(problem, hasFeature("detail", Problem::getDetail, is(nullValue())));
    }

    @Test
    void shouldUseDefaultInstance() {
        final Problem problem = new EmptyProblem();

        assertThat(problem, hasFeature("instance", Problem::getInstance, is(nullValue())));
    }

    @Test
    void shouldUseDefaultParameters() {
        final Problem problem = new EmptyProblem();

        assertThat(problem, hasFeature("parameters", Problem::getParameters, is(emptyMap())));
    }

    @Test
    void simpleAbstractThrowableProblemShouldBeEmpty() {
        final Problem problem = new AbstractThrowableProblem() {
        };

        assertThat(problem, hasFeature(Problem::getType, hasToString("about:blank")));
    }

    @Test
    void shouldRenderEmptyProblem() {
        final Problem problem = Problem.builder().build();
        assertThat(problem, hasToString("about:blank{}"));
    }

    @Test
    void shouldRenderType() {
        final Problem problem = Problem.builder().type(URI.create("my-problem")).build();
        assertThat(problem, hasToString("my-problem{}"));
    }

    @Test
    void shouldRenderTitle() {
        final Problem problem = Problem.builder().title("Not Found").build();
        assertThat(problem, hasToString("about:blank{Not Found}"));
    }

    @Test
    void shouldRenderStatus() {
        final Problem problem = Problem.builder().status(NOT_FOUND).build();
        assertThat(problem, hasToString("about:blank{404, Not Found}"));
    }

    @Test
    void shouldRenderDetail() {
        final Problem problem = Problem.builder().detail("Order 123").build();
        assertThat(problem, hasToString("about:blank{Order 123}"));
    }

    @Test
    void shouldRenderInstance() {
        final Problem problem = Problem.of(NOT_FOUND, URI.create("https://example.org/"));
        assertThat(problem, hasToString("about:blank{404, Not Found, instance=https://example.org/}"));
    }

    @Test
    void shouldRenderFully() {
        final Problem problem = Problem.of(NOT_FOUND, "Order 123", URI.create("https://example.org/"));
        assertThat(problem, hasToString("about:blank{404, Not Found, Order 123, instance=https://example.org/}"));
    }

    @Test
    void shouldRenderCustomDetailAndInstance() {
        final ThrowableProblem problem = Problem.builder()
                .type(URI.create("https://example.org/problem"))
                .title("Not Found")
                .status(NOT_FOUND)
                .detail("Order 123")
                .instance(URI.create("https://example.org/"))
                .build();

        assertThat(problem, hasToString("https://example.org/problem{404, Not Found, Order 123, instance=https://example.org/}"));
    }

    @Test
    void shouldRenderCustomProperties() {
        final ThrowableProblem problem = Problem.builder()
                .type(URI.create("https://example.org/problem"))
                .title("Not Found")
                .status(NOT_FOUND)
                .detail("Order 123")
                .with("foo", "bar")
                .build();

        assertThat(problem, hasToString("https://example.org/problem{404, Not Found, Order 123, foo=bar}"));
    }

    @Test
    void shouldRenderCustomPropertiesWhenPrintingStackTrace() {
        final ThrowableProblem problem = Problem.builder()
                .type(URI.create("https://example.org/problem"))
                .status(NOT_FOUND)
                .with("foo", "bar")
                .build();

        final StringWriter writer = new StringWriter();
        problem.printStackTrace(new PrintWriter(writer));

        assertThat(writer, hasToString(containsString("https://example.org/problem{404, Not Found, foo=bar}")));
    }

}
