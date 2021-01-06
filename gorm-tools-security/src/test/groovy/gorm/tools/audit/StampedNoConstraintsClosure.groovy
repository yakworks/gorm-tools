package gorm.tools.audit

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@AuditStamp
@Entity
@GrailsCompileStatic
class StampedNoConstraintsClosure implements RepoEntity<StampedEntity> {

    String name
    String beforeInsertTest

}
