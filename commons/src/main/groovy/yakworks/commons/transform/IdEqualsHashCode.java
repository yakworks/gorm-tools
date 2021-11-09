package yakworks.commons.transform;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Equals and hash code annotation for Gorm and JPA entites when you want it based on id.
 * doesn't hydrate the proxy when checking it
 * roughtly based on the following
 * https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
 * https://thorben-janssen.com/ultimate-guide-to-implementing-equals-and-hashcode-with-hibernate/
 *
 * @author Joshua Burnett (@basejump)
 *
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("yakworks.commons.transform.ast.IdEqualsHashASTTransformation")
public @interface IdEqualsHashCode {

    /**
     * List of field and/or property names to include within the equals and hashCode calculations.
     * Must not be used if 'excludes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     */
    String[] includes() default {};

    /**
     * List of field and/or property names to use in the hashCode if id is null
     */
    String[] hashKey() default {};

}
