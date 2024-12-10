/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Config from application.yml properties.
 */
@ConfigurationProperties(prefix="app.mail")
@CompileStatic
class MailProps {

    /** whether the mailer is enabled */
    boolean enabled = true

    /** the default domain that set up in mailgun to use, should be one for System stuff and another for AR/Collections stuff*/
    String defaultDomain

    Mailgun mailgun = new Mailgun()

    static class Mailgun {
        boolean enabled = false
        /** the private api key for sdk */
        String privateApiKey

        /** Mailgun Http webhook signing key - used to verify webhook calls **/
        String webhookSigningKey
    }

    //boxes are general setups that can be used for sending emails.
    // Map<String, MailConfig> boxes = [:]
    //
    // static class MailConfig {
    //     /** default from email address */
    //     String from
    //     /** default reply to address */
    //     String replyTo
    // }
}
