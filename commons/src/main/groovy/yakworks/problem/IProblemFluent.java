package yakworks.problem;

import yakworks.api.Result;

import java.net.URI;
import java.util.List;

/**
 * Simple interface for fluent Problem methods
 */
public interface IProblemFluent<E extends IProblemFluent> extends IProblem<E>, Result<E> {
    //Problem builders
    default E detail(String v) { setDetail(v);  return (E)this; }
    default E type(URI v) { setType(v); return (E)this; }
    default E type(String v) { setType(URI.create(v)); return (E)this; }
    default E violations(List<Violation> v) { setViolations(v); return (E)this; }

}
