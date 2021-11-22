package yakworks.problem.exception;

import yakworks.problem.IProblem;

/**
 * An extension of the {@link IProblem} interface for problems that extend {@link Exception}. Since {@link Exception}
 * is a concrete type any class can only extend one exception type. {@link ProblemRuntime} is one choice, but we
 * don't want to force people to extend from this but choose their own super class. For this they can implement this
 * interface and get the same handling as {@link ProblemRuntime} for free. A common use case would be:
 *
 * <pre>{@code
 * public final class OutOfStockException extends BusinessException implements Exceptional
 * }</pre>
 *
 * @see Exception
 * @see IProblem
 * @see ProblemRuntime
 */
public interface Exceptional {

    // Exceptional getCause();

    default Exception propagate() throws Exception {
        throw propagateAs(Exception.class);
    }

    default <X extends Throwable> X propagateAs(final Class<X> type) throws X {
        throw type.cast(this);
    }

}
