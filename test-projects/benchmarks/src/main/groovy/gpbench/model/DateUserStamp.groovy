package gpbench.model

import groovy.transform.CompileStatic

@CompileStatic
trait DateUserStamp {
    Date dateCreated
    Date lastUpdated

    //these don't do anything and are just here to equalize the number of fields
    Long dateCreatedUser
    Long lastUpdatedUser
}

class DateUserStampConstraints implements DateUserStamp{

    static constraints = {
        dateCreated nullable:true,display:false,editable:false,bindable:false
        lastUpdated nullable:true,display:false,editable:false,bindable:false
        dateCreatedUser nullable:true,display:false,editable:false,bindable:false
        lastUpdatedUser nullable:true,display:false,editable:false,bindable:false
    }
}
