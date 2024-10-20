package yakworks.security.audit

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

@AuditStamp
@Entity
@GrailsCompileStatic
class StampedEntity implements RepoEntity<StampedEntity>{

    String name
    String beforeInsertTest

    static mapping = orm {
        cache "nonstrict-read-write"
        table 'stampedEntity'
    }

    static constraints = {
        apiConstraints(delegate)
        name nullable: false
    }

    def beforeInsert(){
        println "in before insert"
        beforeInsertTest = "gotcha"
    }

}
