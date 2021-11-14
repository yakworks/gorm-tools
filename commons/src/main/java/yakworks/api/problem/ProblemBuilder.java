package yakworks.api.problem;

import yakworks.api.ApiStatus;

import javax.annotation.Nullable;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ProblemBuilder {

    private static final Set<String> RESERVED_PROPERTIES = new HashSet<>(Arrays.asList(
            "type", "title", "status", "detail", "instance", "cause"
    ));

    private URI type;
    private String title;
    private ApiStatus status;
    private String detail;
    private URI instance;
    private ThrowableProblem cause;
    private final Map<String, Object> parameters = new LinkedHashMap<>();

    /**
     * @see Problem#builder()
     */
    ProblemBuilder() {

    }

    public ProblemBuilder type(@Nullable final URI type) {
        this.type = type;
        return this;
    }

    public ProblemBuilder title(@Nullable final String title) {
        this.title = title;
        return this;
    }

    public ProblemBuilder status(@Nullable final ApiStatus status) {
        this.status = status;
        if(this.title == null) this.title = status.getReason();
        return this;
    }

    public ProblemBuilder detail(@Nullable final String detail) {
        this.detail = detail;
        return this;
    }

    public ProblemBuilder instance(@Nullable final URI instance) {
        this.instance = instance;
        return this;
    }

    public ProblemBuilder cause(@Nullable final ThrowableProblem cause) {
        this.cause = cause;
        return this;
    }

    /**
     *
     * @param key property name
     * @param value property value
     * @return this for chaining
     * @throws IllegalArgumentException if key is any of type, title, status, detail or instance
     */
    public ProblemBuilder with(final String key, @Nullable final Object value) throws IllegalArgumentException {
        if (RESERVED_PROPERTIES.contains(key)) {
            throw new IllegalArgumentException("Property " + key + " is reserved");
        }
        parameters.put(key, value);
        return this;
    }

    public ThrowableProblem build() {
        return new DefaultProblem(type, title, status, detail, instance, cause, new LinkedHashMap<>(parameters));
    }

}
