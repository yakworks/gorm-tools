package yakworks.api.problem;

import org.junit.jupiter.api.Test;
import yakworks.api.problem.GenericProblems;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class GenericProblemsTest {

    @Test
    void shouldNotBeInstantiable() throws Exception {
        assertThrows(Exception.class, GenericProblems::new);
    }

}
