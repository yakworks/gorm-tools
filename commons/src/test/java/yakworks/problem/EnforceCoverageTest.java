package yakworks.problem;

import org.junit.jupiter.api.Test;
import yakworks.api.ApiStatus;
import yakworks.i18n.MsgKey;
import yakworks.problem.exception.Exceptional;
import yakworks.problem.exception.ProblemRuntime;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static yakworks.api.HttpStatus.BAD_REQUEST;

class EnforceCoverageTest {

    @Test
    void shouldCoverUnreachableThrowStatement() throws Exception {
        assertThrows(FakeProblem.class, () -> {
            throw new FakeProblem().propagate();
        });
    }

    static final class FakeProblem extends Exception implements IProblem.Fluent<FakeProblem>, Exceptional {

        @Override
        public MsgKey getMsg() {
            return MsgKey.ofCode("foo.bar");
        }

        @Override
        public URI getType() {
            return URI.create("about:blank");
        }

        @Override
        public String getTitle() {
            return "Fake";
        }

        @Override
        public ApiStatus getStatus() {
            return BAD_REQUEST;
        }

        @Override
        public ProblemRuntime getCause() {
            return null;
        }

        @Override
        public <X extends Throwable> X propagateAs(final Class<X> type) throws X {
            return type.cast(this);
        }

    }

}
