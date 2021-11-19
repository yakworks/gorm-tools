package yakworks.problem.spi;

import java.util.Collection;

import static java.util.ServiceLoader.load;
import static java.util.stream.StreamSupport.stream;

/**
 * @see java.util.ServiceLoader
 */
public interface StackTraceProcessor {

    // default StackTraceProcessor implementation that just returns all of them
    StackTraceProcessor DEFAULT = elements -> elements;

    //stream wizadry to combine all the services it finds, calling DEFAULT first and then passing up the chain
    StackTraceProcessor COMPOUND = stream(load(StackTraceProcessor.class).spliterator(), false)
        .reduce(DEFAULT, (first, second) -> elements -> second.process(first.process(elements)));

    Collection<StackTraceElement> process(final Collection<StackTraceElement> elements);

}
