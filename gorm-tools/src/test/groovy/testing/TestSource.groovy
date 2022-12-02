package testing

import gorm.tools.model.SourceTrait
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class TestSource implements SourceTrait {
    String name
}
