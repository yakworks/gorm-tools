package gorm.tools.audit

import gorm.tools.repository.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@AuditStamp
@Entity @RepoEntity
@GrailsCompileStatic
class StampedNoConstraintsClosure {

    String name
    String beforeInsertTest

}
