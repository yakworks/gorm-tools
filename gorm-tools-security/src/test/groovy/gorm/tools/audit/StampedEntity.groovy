package gorm.tools.audit

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@AuditStamp
@Entity
@GrailsCompileStatic
class StampedEntity{

    String name
    String beforeInsertTest

    static mapping = {
        cache true
        table 'stampedEntity'
    }

    static constraints = {
        name nullable: false
    }

    def beforeInsert(){
        println "in before insert"
        beforeInsertTest = "gotcha"
    }

}
