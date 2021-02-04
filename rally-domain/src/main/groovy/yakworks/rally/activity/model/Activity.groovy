/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import groovy.transform.CompileDynamic

import yakworks.commons.lang.Validate

import gorm.tools.audit.AuditStampTrait
import gorm.tools.model.Persistable
import gorm.tools.repository.model.GetRepo
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Taggable

@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class Activity implements AuditStampTrait, RepoEntity<Activity>, GetRepo<ActivityRepo>, Taggable<ActivityTag>, Serializable {

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

        static List stringValues() {
            return values().toList()*.name()
        }
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
        //required
        summary nullable: false, blank: false, maxSize: 255
        kind nullable: false
        visibleTo nullable: false

        //not required
        note nullable: true, bindable: true
        parentId nullable: true
        template nullable: true
        task nullable: true
        title nullable: true, maxSize: 255
        source nullable: true, maxSize: 255
        sourceEntity nullable: true, maxSize: 255
        sourceId nullable: true, maxSize: 255
        visibleId nullable: true
        org nullable: false

    }

    List<Attachment> getAttachments() {
        getRepo().attachmentLinkRepo.listItems(this as Persistable)
    }

    boolean hasAttachments() {
        getRepo().hasAttachments(this)
    }

    AttachmentLink addAttachment(Attachment att) {
        Validate.notNull(this.id, "Activity.id")
        return getRepo().linkAttachment(this, att)
    }

    List<ActivityLink> getLinks() {
        ActivityLink.listByActivity(this)
    }

}
