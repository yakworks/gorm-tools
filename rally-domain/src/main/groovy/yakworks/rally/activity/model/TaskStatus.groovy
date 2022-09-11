/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import gorm.tools.model.NameCodeDescription
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.security.audit.AuditStamp

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class TaskStatus implements NameCodeDescription, RepoEntity<TaskStatus>, Serializable{

    String name
    String description
    Task.State state = Task.State.Open //should be Open(0) or Complete(1) here in status

    //the folowing are for what tasks kinds these are good for.
    //FIXME design issue, are these still used? would be better if these were added with more of a tag design concept.
    Boolean validForCall
    Boolean validForEmail
    Boolean validForFax
    Boolean validForMeeting
    Boolean validForParcel
    Boolean validForTask

    Boolean validForSales //can be used from Sales/CRM area of the app
    Boolean validForAr //can be used from A/R & collections area of the app

    static mapping = {
        cache "nonstrict-read-write"
        state enumType: 'identity'
    }

    static constraintsMap = [
        state:[ description: 'The task state this status assigns', nullable: false, required: false, default: 'Open'],
    ]

    static TaskStatus getOPEN() { return this.get(0) }

    static TaskStatus getCOMPLETE() { return this.get(1) }

}

/*
enum TodoStatusEmail {
    Open(TodoState.Open),
    Unsuccessful(TodoState.Complete,false),
    Sent(TodoState.Complete,true),
    Review(TodoState.Open,false)
}

enum TodoStatusFax {
    Open(TodoState.Open),
    Unsuccessful(TodoState.Complete,false),
    Sent(TodoState.Complete,true),
    Review(TodoState.Open,false)
}

enum TodoCallStatus {
    Open(TodoState.Open),
    Complete(TodoState.Complete),
    Unsuccessful(TodoState.Complete,false),
    InProgress(TodoState.Open,false),
    HoldOff(TodoState.Open,false),
    Sent(TodoState.Complete,true),
    Review(TodoState.Open,false),
    LeftMsg(TodoState.Complete,false),
    Void()
}
*/
