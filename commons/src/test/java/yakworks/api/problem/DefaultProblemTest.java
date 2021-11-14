package yakworks.api.problem;

import org.junit.jupiter.api.Test;
import yakworks.api.problem.DefaultProblem;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

import static yakworks.api.problem.Status.BAD_REQUEST;

@SuppressWarnings("ConstantConditions")
final class DefaultProblemTest {

    private final URI type = URI.create("https://example.org/out-of-stock");

    @Test
    void shouldDefaultToAboutBlank() {
        final DefaultProblem problem = new DefaultProblem(null, null, null, null, null, null);
        assertThat(problem.getType(), hasToString("about:blank"));
    }

    @Test
    void shouldImplementProblem() {
        final DefaultProblem problem = new DefaultProblem(type, "Out of Stock", BAD_REQUEST,
                "Item B00027Y5QG is no longer available",
                URI.create("https://example.org/e7203fd2-463b-11e5-a823-10ddb1ee7671"),
                null);

        problem.set("foo", "bar");

        // assertThat(problem, hasFeature("type", Problem::getType, equalTo(type)));
        // assertThat(problem, hasFeature("title", Problem::getTitle, equalTo("Out of Stock")));
        // assertThat(problem, hasFeature("status", Problem::getStatus, equalTo(BAD_REQUEST)));
        // assertThat(problem, hasFeature("detail", Problem::getDetail,
        //         is("Item B00027Y5QG is no longer available")));
        // assertThat(problem, hasFeature("instance", Problem::getInstance,
        //         hasToString("https://example.org/e7203fd2-463b-11e5-a823-10ddb1ee7671")));
        // assertThat(problem, hasFeature(DefaultProblem::getParameters, hasEntry("foo", "bar")));
    }

}
