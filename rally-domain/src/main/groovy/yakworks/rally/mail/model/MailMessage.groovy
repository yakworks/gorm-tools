/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail.model

import java.time.LocalDateTime

import groovy.transform.CompileDynamic

import gorm.tools.repository.model.UuidRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.gorm.hibernate.type.JsonType
import yakworks.security.audit.AuditCreatedTrait

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class MailMessage implements UuidRepoEntity<MailMessage>, AuditCreatedTrait, Serializable {
    // static belongsTo = [Activity]
    UUID id
    /** the state of the Mail */
    MsgState state

    /** used to store source such as the statments date/time, tracking key to pick up and run again*/
    String source

    /** Specify Reply-To address */
    String replyTo

    /** Email address for From header */
    String sendFrom

    /**
     * Email address of the recipient(s). standard email format comma seperated with name in quotes and email in < .. >.
     * example: "Blow, Joe" <joe@joes.com>, john@galt.com
     */
    String sendTo

    /**
     * Same as {@link #sendTo} but for Cc.
     * can also be a single item with emails in standard comma seperated format.
     */
    String cc

    /**
     * Same as {@link #sendTo} but for Bcc.
     * can also be a single item with emails in standard comma seperated format.
     */
    String bcc

    /** Message subject.*/
    String subject

    /** Body of the message.*/
    String body

    /** plain,html or markdown */
    ContentType contentType = ContentType.plain

    /** List of attachment ids*/
    List<Long> attachmentIds

    /** Attachment ids with inline disposition. */
    List<Long> inlineIds

    /** Name of a template stored via template API */
    //String template;

    /** Tags string for mailgun*/
    List<String> tags

    /** The last response or error message from the mail processor.  */
    String msgResponse
    /** when using something like mailgun this is the id returned from the send submit */
    String messageId
    /** The send date, always in Zulu to match server time */
    LocalDateTime sendDate

    @CompileDynamic
    static enum MsgState {
        Queued, //Queued in the system, ready to be sent to mail processor
        Sent, //sent from here vai mailgun or smtp
        Delivered, // if using something like mailgun and we interface, Mailgun sent the email and it was accepted by the recipient email server.
        Opened, // When using something like mailgun with Open tracking enabled
        Complained, //The email recipient clicked on the spam complaint button within their email client.
        Error, //a permanent error occured and message failed, (could be mailgun permanent failure)
        Failed //Mailgun temporary failure
    }



    // static mapping = {
    //     id generator: 'assigned'
    // }


    static mapping = orm {
        //id(generator: "assigned")
        columns(
            id: property(generator: "assigned"),
            sendTo: property(sqlType: 'TEXT'),
            cc: property(sqlType: 'TEXT'),
            bcc: property(sqlType: 'TEXT'),
            subject: property(sqlType: 'TEXT'),
            body: property(sqlType: 'TEXT'),
            attachmentIds: property(type: JsonType, typeParams: [type: ArrayList]),
            inlineIds: property(type: JsonType, typeParams: [type: ArrayList]),
            tags: property(type: JsonType, typeParams: [type: ArrayList]),
        )
    }

    static constraintsMap = [
        sendTo: [
            d: '''
                Email address of the recipient(s). RFC standard email format comma seperated with name in quotes and email in < .. >.
                example: "Blow, Joe" <joe@joes.com>, john@galt.com
            '''.stripIndent(),
            nullable: false, maxSize: 1000
        ],
        cc:[ d: 'cc addys, see `sendTo` for format', maxSize: 1000],
        bcc:[ d: 'bcc addys', maxSize: 1000],
        sendFrom:[ d: 'who its from'],
        replyTo:[ d: 'what to show for replyTo in email'],
        source:[ d: 'any special source indicator'],
        state:[ d: 'the state of the message', nullable: false],
        subject:[ d: 'email message subject', maxSize: 1000],
        body:[ d: 'body of message', maxSize: 65535],
        attachmentIds:[ d: 'ids list of attachments', default: 'plain'],
        inlineIds:[ d: 'plain, html or markdown for whats in body', default: 'plain'],
        sendDate: [ d: 'The last response or error message from the mail processor when availiable.' ],
        msgResponse: [ d: 'The last response or error message from the mail processor when availiable.' ],
        messageId: [ d: 'When using something like mailgun this is the id returned from the send submit']
    ]
}
