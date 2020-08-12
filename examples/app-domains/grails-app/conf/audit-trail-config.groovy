grails {
    plugin {
        audittrail {
            //For a field to be added by the annotation at least on config setting needs to be present for that field.
            createdBy.field = "createdBy" // createdBy is default
            createdBy.constraints = "nullable:true,display:false,editable:false,bindable:false"
            createdDate.field = "createdDate"
            createdDate.constraints = "nullable:true,display:false,editable:false,bindable:false"

            editedBy.field = "editedBy" // createdBy is default
            editedBy.constraints = "nullable:true,display:false,editable:false,bindable:false"
            editedDate.field = "editedDate"
            editedDate.constraints = "nullable:true,display:false,editable:false,bindable:false"
        }
    }
}
