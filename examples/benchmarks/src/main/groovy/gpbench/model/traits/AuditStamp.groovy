package gpbench.model.traits

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@CompileStatic
trait AuditStamp {
    Date createdDate
    Date editedDate

    //these don't do anything and are just here to equalize the number of fields
    Long createdBy
    Long editedBy

    static constraintsMap = [
        createdDate:[ nullable: false, display: false, editable: false, bindable: false],
        editedDate:[ nullable: false, display: false, editable: false, bindable: false],
        createdBy:[ nullable: false, display: false, editable: false, bindable: false],
        editedBy:[ nullable: false, display: false, editable: false, bindable: false]
    ]

}
