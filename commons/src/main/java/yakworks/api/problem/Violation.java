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

    MsgKey getMsgKey();

    default String getCode() {
        return getMsgKey() != null ? getMsgKey().getCode() : null;
    }

    String getField();

    default String getMessage() {
        return null;
    }
}
