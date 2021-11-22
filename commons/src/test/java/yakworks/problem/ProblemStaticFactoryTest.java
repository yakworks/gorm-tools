package yakworks.problem;

import org.junit.jupiter.api.Test;
import yakworks.api.HttpStatus;
import yakworks.problem.exception.ProblemBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

final class ProblemStaticFactoryTest {

    @Test
    void shouldCreateGenericProblem() {
        final IProblem problem = CreateProblem.status(HttpStatus.NOT_FOUND);

        // assertThat(problem, hasFeature("title", Problem::getTitle, equalTo("Not Found")));
        assertThat(problem, hasFeature("status", IProblem::getStatus, equalTo(HttpStatus.NOT_FOUND)));
    }

    @Test
    void shouldCreateGenericProblemWithDetail() {
        final IProblem problem = new ProblemBuilder()
            .status(HttpStatus.NOT_FOUND).detail("Order 123").build();

        // assertThat(problem, hasFeature("title", Problem::getTitle, equalTo("Not Found")));
        assertThat(problem, hasFeature("status", IProblem::getStatus, equalTo(HttpStatus.NOT_FOUND)));
        assertThat(problem, hasFeature("detail", IProblem::getDetail, is("Order 123")));
    }

}
