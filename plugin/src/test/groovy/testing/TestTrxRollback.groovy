package testing

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity @GrailsCompileStatic
class TestTrxRollback {
    String name
    BigDecimal amount

    static constraints = {
        name blank: false, nullable: false
        amount nullable: true
    }
}
