/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import groovy.transform.CompileDynamic

import gorm.tools.audit.AuditStampTrait
import gorm.tools.repository.model.GormRepoEntity
import gorm.tools.source.SourceTrait
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.model.Attachable
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.HasTags
import yakworks.rally.tag.model.Tag

@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class Activity implements AuditStampTrait, SourceTrait, GormRepoEntity<Activity, ActivityRepo>, Attachable, HasTags, Serializable {

    // FIXME https://github.com/9ci/domain9/issues/117 hasMany is still considered evil, change these
    static hasMany = [contacts: Contact]

    Kind kind = Kind.Note
    ActivityNote note

    // Org is here to speed up search arTran activities for customer or custAccount -Activity.findAllByOrg(fromOrg.org)
    // If activity is on org level activityLink has org id as well.
    Org org
    Long parentId //the parent note that this is a comment for.
    String title // the title of this is a task / the subject for an email. Blank otherwise. I question the need for this
    // a 255 char string summary of the activity. Will be the title if its a task and if note it will ends with ... if there is more to the note.
    String summary //don't set this, it will just get overriden during save
    Task task //if this note has a todo task that needs to be /or was/ accomplished
    Attachment template
    //the template that was or will be used to generate this note or the todo's email/fax/letter/report,etc..

    VisibleTo visibleTo = VisibleTo.Everyone
    Long visibleId //the id of the role that can see this note if visibleTo is Role

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
        source column: 'sourceEntity'
        contacts joinTable: [name: 'ActivityContact', key: 'activityId', column: 'personId']
    }

    List<ActivityLink> getLinks() {
        ActivityLink.list(this)
    }

    @Override //Taggable trait
    List<Tag> getTags() {
        ActivityTag.listTags(this)
    }

    // SEE activityApi
    // static constraintsMap = [
    //     links:[ description: 'links for this', validate: false]
    // ]

    // List<ActivityContact> geContacts() {
    //     ActivityContact.findAllWhere(activity: this, [ cache: true ])
    // }

}
