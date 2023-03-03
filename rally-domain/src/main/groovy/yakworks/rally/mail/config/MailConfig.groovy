/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Config from application.yml properteis.
 */
@ConfigurationProperties(prefix="app.mail")
@CompileStatic
class MailConfig {

    /** whether the mailer is enabled */
    boolean enabled = true

    /** the default domain that set up in mailgun to use */
    String defaultDomain

    Mailgun mailgun = new Mailgun()

    static class Mailgun {
        /** the private api key for sdk */
        String privateApiKey

        /** whether the mailer is enabled */
        boolean enabled = false
    }
}