package gpbench.model.traits

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileStatic

@CompileStatic
trait DatesTrait {
    //LocalDate localDate
    Date date1
    LocalDate date2
    LocalDateTime date3
    LocalDate date4

    static constraintsMap = [
        date1:[ d: 'date1'],
        date2:[ d: 'date2'],
        date3:[ d: 'date3'],
        date4:[ d: 'date4']
    ]
}
