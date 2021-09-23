package gpbench.model.traits


import groovy.transform.CompileStatic

@CompileStatic
trait DateUserStamp {

    //These are the default grails/gorm fields to autostamp
    Date dateCreated
    Date lastUpdated

    //these don't do anything and are just here to equalize the number of fields
    Long dateCreatedUser
    Long lastUpdatedUser

    static constraintsMap = [
        dateCreated:[ display: false, editable: false, bindable: false ],
        lastUpdated:[ display: false, editable: false, bindable: false ],
        dateCreatedUser:[ display: false, editable: false, bindable: false ],
        lastUpdatedUser:[ display: false, editable: false, bindable: false ]
    ]
}
