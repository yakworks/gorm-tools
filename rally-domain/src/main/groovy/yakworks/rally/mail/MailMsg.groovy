/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail

import groovy.transform.CompileStatic

/**
 * The object is used for sending messages(emails) using Mailgun API.
 */

@CompileStatic
class MailMsg {

    /**
     * Email address for From header.
     */
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

    /**
     * Message subject.
     */
    String subject

    /**
     * Body of the message. (text version)
     */
    String text

    /**
     * Body of the message. (HTML version)
     */
    String html

    /**
     * File attachment.
     */
    List<File> attachments;

    /**
     * Attachment with inline disposition.
     */
    List<File> inline;

    /**
     * Name of a template stored via template API.
     */
    String template;

    /**
     * Tag string.
     *
     * @see <a href="https://documentation.mailgun.com/en/latest/user_manual.html#tagging-1">Tagging</a>
     */
    List<String> tags;

    /**
     * Specify Reply-To address
     */
    String replyTo;

}
