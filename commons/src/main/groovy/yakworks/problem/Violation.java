package yakworks.problem;

import yakworks.i18n.MsgKey;

/**
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
public interface Violation {

    MsgKey getMsg();

    default String getCode() {
        return getMsg() != null ? getMsg().getCode() : null;
    }

    default String getField(){ return null; }

    default String getMessage() {
        return null;
    }
}
