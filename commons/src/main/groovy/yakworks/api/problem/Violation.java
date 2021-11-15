package yakworks.api.problem;

import yakworks.i18n.MsgKey;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
public interface Violation {

    MsgKey getMsg();

    default String getCode() {
        return getMsg() != null ? getMsg().getCode() : null;
    }

    String getField();

    default String getMessage() {
        return null;
    }
}
