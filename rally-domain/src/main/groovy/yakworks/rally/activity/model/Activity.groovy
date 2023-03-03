/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import groovy.transform.CompileDynamic

import gorm.tools.model.NamedEntity
import gorm.tools.model.SourceTrait
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.model.Attachable
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Taggable
import yakworks.security.audit.AuditStampTrait

@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class Activity implements NamedEntity, AuditStampTrait, SourceTrait, GormRepoEntity<Activity, ActivityRepo>, Attachable, Taggable, Serializable {
    static List<String> toOneAssociations = ['note', 'task']

    Kind kind = Kind.Note

    // Org is here to speed up search arTran activities for customer or custAccount -Activity.findAllByOrg(fromOrg.org)
    // If activity is on org level activityLink has org id as well.
    Org org

    //the parent note that this is a comment for.
    // Long parentId

    /**
     * a 255 char string summary of the activity. This is the subject when its an email
     * Will be the title if its a task and if note it will ends with ... if there is more to the note.
     * don't set this, it will just get overriden during save for taks and notes
     */
    String name

    //if this note has a todo task that needs to be /or was/ accomplished
    Task task

    //the template that was or will be used to generate this note or the todo's email/fax/letter/report,etc..
    // Attachment template

    ActivityNote note

    MailMessage mailMessage

    VisibleTo visibleTo = VisibleTo.Everyone

    //the id of the role that can see this note if visibleTo is Role
    Long visibleId

    /** The priority level generally for an Alert or Log, but can be used for other Activity Kinds */
    AlertLevel level = AlertLevel.Info

    //
    @CompileDynamic
    static enum Kind {
        /** user or collector entered note */
        Note,
        /** something that needs attention, perhaps cust exceed credit limit, triggered high balance KPI, statement sent to bad email, etc */
        Alert,
        /** general info logging for audit trail or history tracking that something was done, when certain fields change etc..*/
        Log,
        /** A Promised Activity such as a Promise to Pay */
        //Promise,
        /** Email sent and will have a linked MailMessage */
        Email,
        //these are synced with a Task kind
        Todo(true), Call(true), Meeting(true) //, Parcel(true)

        boolean isTaskKind

        Kind(boolean isTaskKind = false) { this.isTaskKind = isTaskKind }

        static EnumSet<Kind> getTaskKinds() {
            def taskKinds = values().findAll{ it.isTaskKind }
            return EnumSet.copyOf(taskKinds)
        }
    }

    @CompileDynamic
    static enum AlertLevel {
        Info, // informational, this is the default
        Urgent, // something that requires attention. normally this will be an Alert kind, not a log
        Error, // Something went wrong, example would be if an email is bad, this should be an Alert not a Log but can also be on Emails
        Warn, // A warning, not an error. can either be a Log or an Alert
        Resolved // closed/completed/resolved, action has been taken dont notify about it anymore. This is updated by user generally
    }

    @CompileDynamic
    enum VisibleTo { Company, Everyone, Owner, Role }

    static mapping = {
        name column: 'summary' //FIXME rename column
        note column: 'noteId'
        org column: 'orgId'
        //template column: 'templateId'
        task column: 'taskId'
        mailMessage column: 'mailMessageId'
        // source column: 'sourceEntity'
    }

    static Map constraintsMap = [
        contacts: [
            d: 'The contacts associated with this activity.', validate: false
        ],
        kind: [
            d: 'The type of the activity, certain kinds are only valid for a Task.',
            nullable: false,
            default: 'Note',
            opai: 'CR'
        ],
        links: [
            d: 'The entities this is linked to.',
            validate: false
        ],
        note: [
            d: 'A note for this activity. Name will be built from this'
        ],
        level: [
            d: 'The priority level generally for an Alert or Log, but can be used for other Activity Kinds', nullable: false, default: "Info"
        ],
        org: [
            d: 'The Org this activity belongs to',
            nullable: false
        ],
        source: [
            d: ''' The source description for where this activity came from.
                The gorm domain name of the record that generated this such as CollectionStep, Promise'''
        ],
        sourceId: [
            nullable: true,
            d: 'The id from the outside source or of the collection step, promise or some future workflow template record that generated this'
        ],
        name: [
            d: 'The short name or a 255 char string summary of the activity, if note it will ends with ... if there is more to the note.',
            maxSize: 255
        ],
        task: [
            d: 'The task info if this is a task kind'
        ],
        // template: [
        //     d: 'The template that was or will be used to generate this note or the tasks email/fax/letter/report,etc..'
        // ],
        visibleId: [
            d: 'The id fo the role or group this is visible to if set to role',
        ],
        visibleTo: [
            d: 'Who can see this activity. Defaults to Everyone',
            default: 'Everyone',
            nullable: false
        ]
    ]

    List<ActivityLink> getLinks() {
        ActivityLink.list(this)
    }

    List<Contact> getContacts() {
        ActivityContact.listContacts(this)
    }

}
