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
     *
     * @return an absolute URI that identifies this specific problem
     */
    // @Nullable
    // default URI getInstance() { return null; }

    // static ProblemTrait empty() {
    //     return CreateProblem.create();
    // }
    //
    // static ProblemTrait withCode(String code) {
    //     return CreateProblem.code(code);
    // }
    //
    // static ProblemTrait of(Object value) {
    //     return CreateProblem.of(value);
    // }
    //
    // static ProblemTrait of(String code, Object args) {
    //     return CreateProblem.code(code, args);
    // }
    //
    // static ProblemTrait withMsg(MsgKey code) {
    //     return CreateProblem.msg(code);
    // }
    //
    // static ProblemTrait withDetail(String detail) {
    //     return CreateProblem.detail(detail);
    // }

    //
    // static ProblemTrait of(Throwable cause) {
    //     return ProblemBuilder.create(cause);
    // }

    // static ThrowableProblem of(final ApiStatus status, final URI instance) {
    //     return create().status(status).instance(instance);
    // }
    //
    // static ThrowableProblem of(final ApiStatus status, final String detail, final URI instance) {
    //     return create().status(status).detail(detail).instance(instance);
    // }

}
