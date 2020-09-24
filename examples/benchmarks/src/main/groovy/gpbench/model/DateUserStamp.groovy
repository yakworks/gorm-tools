package gpbench.model

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@CompileStatic
trait DateUserStamp {

    //These are the default grails/gorm fields to autostamp
    Date dateCreated
    Date lastUpdated

    //these don't do anything and are just here to equalize the number of fields
    Long dateCreatedUser
    Long lastUpdatedUser

    @CompileDynamic
    static DateUserStampConstraints(Object delegate) {
        def c = {
            dateCreated nullable: true, display: false, editable: false, bindable: false
            lastUpdated nullable: true, display: false, editable: false, bindable: false
            dateCreatedUser nullable: true, display: false, editable: false, bindable: false
            lastUpdatedUser nullable: true, display: false, editable: false, bindable: false
        }
        c.delegate = delegate
        c()
    }
}

class DateUserStampConstraints implements DateUserStamp {

    static constraints = {
        dateCreated nullable: true, display: false, editable: false, bindable: false
        lastUpdated nullable: true, display: false, editable: false, bindable: false
        dateCreatedUser nullable: true, display: false, editable: false, bindable: false
        lastUpdatedUser nullable: true, display: false, editable: false, bindable: false
    }
}
