/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail

import javax.inject.Inject

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import yakworks.api.Result
import yakworks.rally.mail.config.MailProps

/**
 * Basic service for MailGun.
 * works for a basic send, recomended to use the mailgunMessagesApi for anything outside of a simple send
 * and use the mailgunEventsApi for filtering  anything outside of a basic event calls, here for baseline examples of what can be done
 */
@Slf4j
@CompileStatic
abstract class EmailService {

    @Inject MailProps mailProps

    /**
     * calls mailgunMessagesApi.sendMessage
     */
    abstract Result send(String domain, MailTo mailMsg)

    /**
     * calls mailgunMessagesApi.sendMessage using the MailgunConfig.defaultDomain
     */
    Result send(MailTo mailMsg){
        return send(mailProps.defaultDomain, mailMsg)
    }

    /**
     * calls mailgunMessagesApi.sendMessage using the MailgunConfig.defaultDomain
     */
    Result sendMessages(List<MailTo> msgList){
        for(MailTo mailMsg : msgList){
            send(mailMsg)
        }
        return Result.OK()
    }

    /**
     * The class is used for sending messages(emails) using the Email and Mailgun API.
     */
    @ToString(includePackage = false, includeNames = true, ignoreNulls = true, excludes = "attachments,inline")
    static class MailTo {

        /** Email address for From header */
        String from

        /**
         * Email address of the recipient(s).
         * can also be a single item with emails in the standard comma seperated format.
         */
        List<String> to

        /**
         * Same as {@link #to} but for Cc.
         * can alos be a single item with emails in standard comma seperated format.
         */
        List<String> cc

        /**
         * Same as {@link #to} but for Bcc.
         * can also be a single item with emails in standard comma seperated format.
         */
        List<String> bcc

        /** Message subject.*/
        String subject

        /** Body of the message. (text version) */
        String text

        /** Body of the message. (HTML version) */
        String html

        /** File attachment */
        List<File> attachments;

        /** Attachment with inline disposition. */
        List<File> inline;

        /** Name of a template stored via template API */
        String template;

        /** Tag string for mailgun*/
        List<String> tags;

        /** Specify Reply-To address */
        String replyTo;
    }
}
