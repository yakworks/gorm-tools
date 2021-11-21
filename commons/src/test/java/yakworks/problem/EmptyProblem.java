package yakworks.problem;

import yakworks.i18n.MsgKey;

public final class EmptyProblem implements IProblem<EmptyProblem> {

    @Override
    public MsgKey getMsg() {
        return MsgKey.ofCode("some.problem.key");
    }
}
