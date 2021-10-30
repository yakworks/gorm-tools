/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import groovy.transform.CompileDynamic

import gorm.tools.audit.AuditStampTrait
import gorm.tools.model.SourceTrait
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.model.Attachable
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Taggable

@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class Activity implements AuditStampTrait, SourceTrait, GormRepoEntity<Activity, ActivityRepo>, Attachable, Taggable, Serializable {
    // static transients = ['hasAttachments']
    // XXX Sync changes with database
    //  - databse has a whole bunch of fields that are not needed, like forCustome, templateId, etc...
    //  - remove title as its just a dup of summary

    Kind kind = Kind.Note

    // Org is here to speed up search arTran activities for customer or custAccount -Activity.findAllByOrg(fromOrg.org)
    // If activity is on org level activityLink has org id as well.
    Org org
    //the parent note that this is a comment for.
    Long parentId

    // a 255 char string summary of the activity. Will be the title if its a task and if note it will ends with ... if there is more to the note.
    String summary //don't set this, it will just get overriden during save

    //if this note has a todo task that needs to be /or was/ accomplished
    Task task

    //the template that was or will be used to generate this note or the todo's email/fax/letter/report,etc..
    Attachment template

    ActivityNote note

    VisibleTo visibleTo = VisibleTo.Everyone

    //the id of the role that can see this note if visibleTo is Role
    Long visibleId

    //
    @CompileDynamic
    static enum Kind {
        Note, Comment, Promise,
        Todo(true), Call(true), Meeting(true), Email(true), Fax(true), Parcel(true)

        boolean isTaskKind

        Kind(boolean isTaskKind = false) { this.isTaskKind = isTaskKind }

        static EnumSet<Kind> getTaskKinds() {
            def taskKinds = values().findAll{ it.isTaskKind }
            return EnumSet.copyOf(taskKinds)
        }
    }

    @CompileDynamic
    enum VisibleTo { Company, Everyone, Owner, Role }

    static mapping = {
        note column: 'noteId'
        org column: 'orgId'
        template column: 'templateId'
        task column: 'taskId'
        source column: 'sourceEntity' //XXX why are we mapping this like this
    }

    List<ActivityLink> getLinks() {
        ActivityLink.list(this)
    }

    List<Contact> getContacts() {
        ActivityContact.listContacts(this)
    }

    // SEE activityApi
    // static constraintsMap = [
    //     links:[ description: 'links for this', validate: false]
    // ]

}
