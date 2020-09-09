package gorm.tools.audit;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;

/**
 * Annotation used for GORM domain classes that should have the following
 * properties createdBy/createdDate/editedBy/editedDate
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@GroovyASTTransformationClass("gorm.tools.audit.ast.AuditStampASTTransformation")
public @interface AuditStamp {
}
