/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail.model

import groovy.transform.CompileDynamic

import gorm.tools.hibernate.type.JsonType
import gorm.tools.repository.model.UuidGormRepo
import gorm.tools.repository.model.UuidRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.security.audit.AuditCreatedTrait

@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class MailMessage implements UuidRepoEntity<MailMessage, UuidGormRepo<MailMessage>>, AuditCreatedTrait, Serializable {
    // static belongsTo = [Activity]
    UUID id
    /** the state of the Mail */
    MailState state

    /** used to store tracking key to pick up and run again*/
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

    @CompileDynamic
    static enum MailState {
        Queued, //Queued in our system, to be sent
        Sent, //sent from here vai mailgun or smtp
        Delivered, // if using something like mailgun and we interface, Mailgun sent the email and it was accepted by the recipient email server.
        Opened, // When using something like mailgun with Open tracking enabled
        Complained //The email recipient clicked on the spam complaint button within their email client.
    }

    @CompileDynamic
    static enum ContentType {
        plain, html, markdown
    }


    // static mapping = {
    //     id generator: 'assigned'
    // }


    //@CompileDynamic
    static mapping = {
        id generator: "assigned"
        sendTo sqlType:'TEXT'
        cc sqlType:'TEXT'
        bcc sqlType:'TEXT'
        subject sqlType:'TEXT'
        body sqlType:'TEXT'
        subject sqlType:'TEXT'
        attachmentIds type: JsonType, params: [type: ArrayList]
        inlineIds type: JsonType, params: [type: ArrayList]
        tags type: JsonType, params: [type: ArrayList]
    }

    static constraintsMap = [
        sendTo:[
            d: '''
                Email address of the recipient(s). RFC standard email format comma seperated with name in quotes and email in < .. >.
                example: "Blow, Joe" <joe@joes.com>, john@galt.com
            '''.stripIndent(),
            nullable: false
        ],
        cc:[ d: 'cc addys, see `sendTo` for format'],
        bcc:[ d: 'bcc addys'],
        sendFrom:[ d: 'who its from', maxSize: 255],
        replyTo:[ d: 'what to show for replyTo in email', maxSize: 255],
        source:[ d: 'any special source indicator', maxSize: 255],
        state:[ d: 'the state of the message', nullable: false],
        subject:[ d: 'email message subject'],
        body:[ d: 'body of message'],
        attachmentIds:[ d: 'ids list of attachments', default: 'plain'],
        inlineIds:[ d: 'plain, html or markdown for whats in body', default: 'plain'],
    ]
}
