package yakworks.api.problem;

import yakworks.api.ApiStatus;
import yakworks.api.Result;
import yakworks.i18n.MsgKey;

import javax.annotation.Nullable;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * {@link Problem} instances are required to be immutable.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7807">RFC 7807: Problem Details for HTTP APIs</a>
 */
public interface Problem extends Result {

    @Override default Boolean getOk() { return false; }

    //this should be rendered to json if type is null
    URI DEFAULT_TYPE = URI.create("about:blank");

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
    // default E type(URI v) { setType(v); return (E)this; }
    // default E type(String v) { setType(URI.create(v)); return (E)this; }

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
     *
     * @return an absolute URI that identifies this specific problem
     */
    @Nullable
    default URI getInstance() { return null; }

    /**
     * Optional, additional attributes of the problem. Implementations can choose to ignore this in favor of concrete,
     * typed fields.
     *
     * @return additional parameters
     */
    // default Map<String, Object> getParameters() {
    //     return Collections.emptyMap();
    // }

    static ProblemBuilder builder() {
        return new ProblemBuilder();
    }

    static ProblemException create() {
        return new ProblemException();
    }

    static ProblemException of(final ApiStatus status) {
        return builder().status(status).build();
    }

    static ProblemException of(final ApiStatus status, final String detail) {
        return builder().status(status).detail(detail).build();
    }

    static ProblemException of(MsgKey msg) {
        return builder().msg(msg).build();
    }

    static ProblemException of(String code, Object args) {
        return builder().msg(MsgKey.of(code, args)).build();
    }

    // static ThrowableProblem of(final ApiStatus status, final URI instance) {
    //     return create().status(status).instance(instance);
    // }
    //
    // static ThrowableProblem of(final ApiStatus status, final String detail, final URI instance) {
    //     return create().status(status).detail(detail).instance(instance);
    // }

}
