package testing

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity @GrailsCompileStatic
class Org {
    int id
    String name
    Boolean isActive = false
    BigDecimal amount
    BigDecimal amount2
    Location location
    String secondName
    Date date
    String nameFromRepo

    String event

    static List quickSearchFields = ["name"]

    static constraints = {
        name blank: true, nullable: true
        isActive nullable: true
        amount nullable: true
        amount2 nullable: true
        date nullable: true
        secondName nullable: true
        nameFromRepo nullable: true
        event nullable: true, blank: false
    }
}
