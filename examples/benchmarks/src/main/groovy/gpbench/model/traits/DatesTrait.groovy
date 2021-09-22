package gpbench.model.traits

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import java.time.LocalDate
import java.time.LocalDateTime

@CompileStatic
trait DatesTrait {
    //LocalDate localDate
    Date date1
    LocalDate date2
    LocalDateTime date3
    LocalDate date4

    @CompileDynamic
    static DatesTraitConstraints(Object delegate) {
        def c = {
            date1 nullable: true
            date2 nullable: true
            date3 nullable: true
            date4 nullable: true
        }
        c.delegate = delegate
        c()
    }
}
