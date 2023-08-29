/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail.model

import java.nio.file.Path

import groovy.transform.CompileStatic

import yakworks.rally.mail.model.ContentType

/**
 * A value object to represent a specific type of email to be sent (receipt, statement, etc...)
 * For spring configs as well as to be passed into the CommonMailer to send emails.
 */
@CompileStatic
class MailerTemplate {
    boolean enabled = false
    /** the send it from address, should match domain used in mailgun, can be set from higher level config defaults  */
    String sendFrom
    /** the reply to address, can be set from higher level config defaults */
    String replyTo

    /** subject template */
    String subject

    /** Handlebars tempalate for body */
    String body

    /** the template file */
    Path bodyTemplate

    /** for future use to specify the format of the body */
    ContentType contentType = ContentType.plain

    /** tags to put on mailgun so it can be grouped and easy to see what part of app its coming from */
    List<String> tags
}
