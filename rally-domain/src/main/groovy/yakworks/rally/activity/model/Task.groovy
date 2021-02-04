/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import java.time.LocalDateTime

import groovy.transform.CompileDynamic

import gorm.tools.audit.AuditStamp
import gorm.tools.model.IdEnum
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.orgs.model.Contact

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class Task implements RepoEntity<Task>, Serializable {
    static transients = ['completedByName', 'assignedToName']
    static belongsTo = [activity: Activity]

    Long completedBy //who completed this. System is 1
    LocalDateTime completedDate  //when was this completed
    String docTag
    // this will be the template.name or can be entered from a list or freeform if we need to for some future enhancment
    LocalDateTime dueDate //the date this is due. the date it was generated for Collectionsteps
    Integer priority //10 is Critical, 20 High, 30 Medium, 40 Low
    State state = State.Open //0 = open/not complete. 1 = Complete. 3 = Void(Deleted)
    TaskStatus status
    //String     title // the title or subject for this task
    TaskType taskType
    //who is responsible for making sure this thing is done. null means anyone.
    //normally the same as createdBy unless I am creating an activity for someone else to complete
    Long userId


    @CompileDynamic //bug in GrailsCompileStatic
    static enum State implements IdEnum<State, Integer> {
        Open(0), Complete(1), Draft(2), Void(3)
        Integer id

        State(Integer id) {
            this.id = id
        }
    }

    static mapping = {
        state column: 'state', enumType: 'identity'
        status column: 'statusId'
        taskType column: 'taskTypeId'
    }

    static constraints = {
        dueDate nullable: false
        state nullable: false
        status nullable: false
        taskType nullable: false
        //title         nullable:false

        /*optional */
        completedBy nullable: true
        completedDate nullable: true
        docTag nullable: true, maxSize: 255
        priority nullable: true
        userId nullable: true
    }

    void setupDefaultStatus() {
        if (!status) status = TaskStatus.OPEN
    }

    String getCompletedByName() {
        Contact.get(completedBy)?.name
    }

    String getAssignedToName() {
        Contact.get(userId)?.name
    }

}
