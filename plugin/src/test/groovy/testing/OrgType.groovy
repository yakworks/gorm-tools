package testing

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class OrgType {
    String name

    static constraints = {
        name blank: false, nullable: false
    }
}
