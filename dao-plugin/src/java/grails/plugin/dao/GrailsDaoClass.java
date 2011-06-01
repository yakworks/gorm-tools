package grails.plugin.dao;
import org.codehaus.groovy.grails.commons.InjectableGrailsClass;
/**
 * @author Joshua Burnett
 */
public interface GrailsDaoClass extends InjectableGrailsClass {

    /**
     * Service should be configured with transaction demarcation.
     *
     * @return configure with transaction demarcation
     */
    boolean isTransactional();
}
