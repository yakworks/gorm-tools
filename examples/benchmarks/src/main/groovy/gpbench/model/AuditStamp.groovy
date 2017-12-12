package gpbench.model

import groovy.transform.CompileStatic

@CompileStatic
trait AuditStamp {
    Date createdDate
    Date editedDate

    //these don't do anything and are just here to equalize the number of fields
    Long createdBy
    Long editedBy
}

class AuditStampConstraints implements AuditStamp{

    static constraints = {
        createdDate nullable:false,display:false,editable:false,bindable:false
        editedDate nullable:false,display:false,editable:false,bindable:false
        createdBy nullable:false,display:false,editable:false,bindable:false
        editedBy nullable:false,display:false,editable:false,bindable:false
    }
}
