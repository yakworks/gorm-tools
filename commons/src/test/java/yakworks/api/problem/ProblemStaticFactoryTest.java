package yakworks.api.problem;

import org.junit.jupiter.api.Test;
import yakworks.api.HttpStatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

final class ProblemStaticFactoryTest {

    @Test
    void shouldCreateGenericProblem() {
        final Problem problem = Problem.of(HttpStatus.NOT_FOUND);

        assertThat(problem, hasFeature("title", Problem::getTitle, equalTo("Not Found")));
        assertThat(problem, hasFeature("status", Problem::getStatus, equalTo(HttpStatus.NOT_FOUND)));
    }

    @Test
    void shouldCreateGenericProblemWithDetail() {
        final Problem problem = Problem.of(HttpStatus.NOT_FOUND, "Order 123");

        assertThat(problem, hasFeature("title", Problem::getTitle, equalTo("Not Found")));
        assertThat(problem, hasFeature("status", Problem::getStatus, equalTo(HttpStatus.NOT_FOUND)));
        assertThat(problem, hasFeature("detail", Problem::getDetail, is("Order 123")));
    }

}
