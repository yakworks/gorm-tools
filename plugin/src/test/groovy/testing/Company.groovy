package testing

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity @GrailsCompileStatic
class Company {
    int id
    String name
    String num
    Location location


    static constraints = {
        name blank: true, nullable: true
        num blank: true, nullable: true
    }
}
