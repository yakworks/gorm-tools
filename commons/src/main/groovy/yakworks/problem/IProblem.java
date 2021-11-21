package yakworks.problem;

import yakworks.api.Result;
import yakworks.i18n.MsgKey;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Simple interface for problem getters
 *
 * @see <a href="https://tools.ietf.org/html/rfc7807">RFC 7807: Problem Details for HTTP APIs</a>
 */
public interface IProblem extends Result {

    @Override default Boolean getOk() { return false; }

    @Override
    default MsgKey getMsg() {
        return MsgKey.ofCode("problem.blank");
    }

    /**
     * An absolute URI that identifies the problem type. When dereferenced,
     * it SHOULD provide human-readable documentation for the problem type
     * (e.g., using HTML). When this member is not present, its value is
     * assumed to be "about:blank".
     *
     * @return an absolute URI that identifies this problem's type
     */
    default URI getType() { return null;}
    default void setType(URI v){}

    /**
     * A human readable explanation specific to this occurrence of the problem.
     *
     * @return A human readable explaination of this problem
     */
    default String getDetail() {
        return null;
    }
    default void setDetail(String v){}
    // default E detail(String v) { setDetail(v);  return (E)this; }

    /**
     * The list of constraint violations or any others
     */
    default List<Violation> getViolations(){
        return Collections.emptyList();
    }
    default void setViolations(List<Violation> v){}
    // default E violations(List<Violation> v) { setViolations(v); return (E)this; }

    /**
     * An absolute URI that identifies the specific occurrence of the problem.
     * It may or may not yield further information if dereferenced.
     */
    default URI getInstanceURI() { return null; }

    interface Fluent<E extends Fluent> extends IProblem, Result.Fluent<E> {
        //Problem builders
        default E detail(String v) { setDetail(v);  return (E)this; }
        default E type(URI v) { setType(v); return (E)this; }
        default E type(String v) { setType(URI.create(v)); return (E)this; }
        default E violations(List<Violation> v) { setViolations(v); return (E)this; }

    }
}
