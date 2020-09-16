package gorm.tools.audit

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@AuditStamp
@Entity
@GrailsCompileStatic
class StampedNoConstraintsClosure {

    String name
    String beforeInsertTest

}
