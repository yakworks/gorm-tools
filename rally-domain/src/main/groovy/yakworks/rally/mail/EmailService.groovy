/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail

import javax.inject.Inject
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import yakworks.api.Result
import yakworks.api.problem.data.DataProblem
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

    /**
     * Verify if given email is valid
     * Accepts single or comma seperated list of emails
     */
    Result validateEmail(String mail) {
        Result OK = Result.OK()
        //Split by comma, only if comma is not in quotes, eg  "Blow, Joe" <joeb@9ci.com>
        //Based on : https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
        String[] mails = mail.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)", -1)
        for(String m : mails) {
            try {
                InternetAddress address = new InternetAddress(m)
                address.validate()
            } catch(AddressException ex) {
                return DataProblem.ofPayload(m).title("Invalid email address").detail("Invalid email address : $m")
            }
        }
        return OK
    }
}
