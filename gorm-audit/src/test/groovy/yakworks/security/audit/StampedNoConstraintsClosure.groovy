package yakworks.security.audit

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@AuditStamp
@Entity
@GrailsCompileStatic
class StampedNoConstraintsClosure implements RepoEntity<StampedNoConstraintsClosure> {

    String name
    String beforeInsertTest

}
