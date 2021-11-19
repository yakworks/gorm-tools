package yakworks.problem;

import yakworks.i18n.MsgKey;
import yakworks.problem.Problem;

public final class EmptyProblem implements Problem {

    @Override
    public MsgKey getMsg() {
        return MsgKey.of("some.problem.key");
    }
}
