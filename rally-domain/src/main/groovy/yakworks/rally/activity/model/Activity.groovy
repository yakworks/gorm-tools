/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import groovy.transform.CompileDynamic

import gorm.tools.audit.AuditStampTrait
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
class Activity implements AuditStampTrait, GormRepoEntity<Activity, ActivityRepo>, Attachable, Taggable<ActivityTag>, Serializable {

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

    String source //the source if this is from an outside source
    String sourceEntity  //The gorm domain name of the record that generated this. Ex: CollectionStep, Promise
    String sourceId
    //The id from the outside source or of the collection step, promise or some future workflow template record that generated this

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
        contacts joinTable: [name: 'ActivityContact', key: 'activityId', column: 'personId']
        // attachments joinTable: [name: 'ActivityAttachment', key: 'ActivityId', column: 'AttachmentId']
    }

    static constraints = {
        AuditStampTraitConstraints(delegate)
        //TaggableConstraints(delegate)
        ActivityConstraints(delegate)
    }

    @CompileDynamic
    static ActivityConstraints(Object delegate) {
        def c = {
            // summary d: 'A 255 char string summary of the activity.
            // Will be the title if its a task and if note it will ends with ... if there is more to the note.',
            //     nullable: false, blank: false, maxSize: 255

            kind d: 'The type of the activity, certain kinds oare only valid for a Task',
                nullable: false, api:[required: false]

            note d: 'A note for this activity. Summary will be built from this',
                nullable: true, bindable: true

            parentId d: 'The parent note that this is a comment for',
                nullable: true

            template d: 'The template that was or will be used to generate this note or the tasks email/fax/letter/report,etc..',
                nullable: true

            task d: 'The task info if this is task kind',
                nullable: true

            title d: 'The title for this, optional as summary will work',
                nullable: true, maxSize: 255

            source nullable: true, maxSize: 255
            sourceEntity nullable: true, maxSize: 255
            sourceId nullable: true, maxSize: 255
            visibleTo description: 'Defaults to Everyone',
                nullable: false, required: false
            visibleId nullable: true
            org nullable: false
            links nullable: true
        }
        c.delegate = delegate
        c()
    }

    List<ActivityLink> getLinks() {
        ActivityLink.listByActivity(this)
    }

}
