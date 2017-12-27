package gpbench.model

import groovy.transform.CompileStatic

import java.time.LocalDate

@CompileStatic
trait DatesTrait {
    //LocalDate localDate
    Date date1
    LocalDate date2
    Date date3
    LocalDate date4
}

class DatesTraitConstraints implements DatesTrait {

    static constraints = {
        date1 nullable: true
        date2 nullable: true
        date3 nullable: true
        date4 nullable: true
    }
}
