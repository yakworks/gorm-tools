package yakworks.api.problem;

import yakworks.api.ApiStatus;
import yakworks.api.Result;

import javax.annotation.Nullable;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * {@link Problem} instances are required to be immutable.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7807">RFC 7807: Problem Details for HTTP APIs</a>
 */
public interface Problem extends Result {

    URI DEFAULT_TYPE = URI.create("about:blank");

    /**
     * An absolute URI that identifies the problem type. When dereferenced,
     * it SHOULD provide human-readable documentation for the problem type
     * (e.g., using HTML). When this member is not present, its value is
     * assumed to be "about:blank".
     *
     * @return an absolute URI that identifies this problem's type
     */
    default URI getType() { return DEFAULT_TYPE;}
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
    default Map<String, Object> getParameters() {
        return Collections.emptyMap();
    }

    static ProblemBuilder builder() {
        return new ProblemBuilder();
    }

    static ThrowableProblem of(final ApiStatus status) {
        return builder().status(status).build();
    }

    static ThrowableProblem of(final ApiStatus status, final String detail) {
        return builder().status(status).detail(detail).build();
    }

    static ThrowableProblem of(final ApiStatus status, final URI instance) {
        return builder().status(status).instance(instance).build();
    }

    static ThrowableProblem of(final ApiStatus status, final String detail, final URI instance) {
        return builder().status(status).detail(detail).instance(instance).build();
    }

    /**
     * Specification by example:
     * <pre>{@code
     *   // Returns "about:blank{404, Not Found}"
     *   Problem.valueOf(NOT_FOUND).toString();
     *
     *   // Returns "about:blank{404, Not Found, Order 123}"
     *   Problem.valueOf(NOT_FOUND, "Order 123").toString();
     *
     *   // Returns "about:blank{404, Not Found, instance=https://example.org/}"
     *   Problem.valueOf(NOT_FOUND, URI.create("https://example.org/")).toString();
     *
     *   // Returns "about:blank{404, Not Found, Order 123, instance=https://example.org/"}
     *   Problem.valueOf(NOT_FOUND, "Order 123", URI.create("https://example.org/")).toString();
     *
     *   // Returns "https://example.org/problem{422, Oh, oh!, Crap., instance=https://example.org/problem/123}
     *   Problem.builder()
     *       .withType(URI.create("https://example.org/problem"))
     *       .withTitle("Oh, oh!")
     *       .withStatus(UNPROCESSABLE_ENTITY)
     *       .withDetail("Crap.")
     *       .withInstance(URI.create("https://example.org/problem/123"))
     *       .build()
     *       .toString();
     * }</pre>
     *
     * @param problem the problem
     * @return a string representation of the problem
     */
    static String toString(final Problem problem) {
        final Stream<String> parts = Stream.concat(
                Stream.of(
                        problem.getStatus() == null ? null : String.valueOf(problem.getStatus().getCode()),
                        problem.getTitle(),
                        problem.getDetail(),
                        problem.getInstance() == null ? null : "instance=" + problem.getInstance()),
                problem.getParameters()
                        .entrySet().stream()
                        .map(Map.Entry::toString))
                .filter(Objects::nonNull);

        return problem.getType().toString() + "{" + parts.collect(joining(", ")) + "}";
    }

}
