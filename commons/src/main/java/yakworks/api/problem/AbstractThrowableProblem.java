package yakworks.api.problem;

import yakworks.api.ApiStatus;

import javax.annotation.Nullable;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractThrowableProblem extends ThrowableProblem {

    private final URI type;
    private final String title;
    private final ApiStatus status;
    private final String detail;
    private final URI instance;
    private final Map<String, Object> parameters;

    protected AbstractThrowableProblem() {
        this(null);
    }

    protected AbstractThrowableProblem(
            @Nullable final URI type) {
        this(type, null);
    }

    protected AbstractThrowableProblem(
            @Nullable final URI type,
            @Nullable final String title) {
        this(type, title, null);
    }

    protected AbstractThrowableProblem(
            @Nullable final URI type,
            @Nullable final String title,
            @Nullable final ApiStatus status) {
        this(type, title, status, null);
    }

    protected AbstractThrowableProblem(
            @Nullable final URI type,
            @Nullable final String title,
            @Nullable final ApiStatus status,
            @Nullable final String detail) {
        this(type, title, status, detail, null);
    }

    protected AbstractThrowableProblem(
            @Nullable final URI type,
            @Nullable final String title,
            @Nullable final ApiStatus status,
            @Nullable final String detail,
            @Nullable final URI instance) {
        this(type, title, status, detail, instance, null);
    }

    protected AbstractThrowableProblem(
            @Nullable final URI type,
            @Nullable final String title,
            @Nullable final ApiStatus status,
            @Nullable final String detail,
            @Nullable final URI instance,
            @Nullable final ThrowableProblem cause) {
        this(type, title, status, detail, instance, cause, null);
    }

    protected AbstractThrowableProblem(
            @Nullable final URI type,
            @Nullable final String title,
            @Nullable final ApiStatus status,
            @Nullable final String detail,
            @Nullable final URI instance,
            @Nullable final ThrowableProblem cause,
            @Nullable final Map<String, Object> parameters) {
        super(cause);
        this.type = Optional.ofNullable(type).orElse(DEFAULT_TYPE);
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.parameters = Optional.ofNullable(parameters).orElseGet(LinkedHashMap::new);
    }

    @Override
    public URI getType() {
        return type;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public ApiStatus getStatus() {
        return status;
    }

    @Override
    public String getDetail() {
        return detail;
    }

    @Override
    public URI getInstance() {
        return instance;
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * This is required to workaround missing support for com.fasterxml.jackson.annotation.JsonAnySetter on
     * constructors annotated with com.fasterxml.jackson.annotation.JsonCreator.
     *
     * @param key   the custom key
     * @param value the custom value
     * @see <a href="https://github.com/FasterXML/jackson-databind/issues/562">Jackson Issue 562</a>
     */
    void set(final String key, final Object value) {
        parameters.put(key, value);
    }

}
