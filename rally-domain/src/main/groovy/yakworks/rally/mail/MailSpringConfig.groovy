/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail

import groovy.transform.CompileStatic

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile

import yakworks.rally.mail.mailgun.MailgunService
import yakworks.rally.mail.testing.TestMailService

@Configuration @Lazy
@ConfigurationPropertiesScan([ "yakworks.rally.mail.config" ])
//@ComponentScan(['nine.mailgun']) //scan for the Repos, anything other beans should be specified here
@CompileStatic
class MailSpringConfig {

    @ConditionalOnProperty(value="app.mail.mailgun.enabled", havingValue = "false", matchIfMissing = true)
    @Bean
    EmailService emailService() {
        return new TestMailService()
    }

    //@Autowired MailConfig mailConfig
    @Bean
    MailMessageSender mailMessageSender() {
        return new MailMessageSender()
    }

    @Configuration
    @ConditionalOnProperty(value="app.mail.mailgun.enabled", havingValue = "true")
    static class MailgunBeans {

        //@Profile("!test")
        @Bean
        EmailService emailService() {
            return new MailgunService()
        }

        // @Bean
        // MailgunMessagesApi mailgunMessagesApi(MailConfig mailConfig) {
        //     return MailgunClient.config(mailConfig.mailgun.privateApiKey)
        //         .createApi(MailgunMessagesApi.class);
        // }
        //
        // @Bean
        // MailgunEventsApi mailgunEventsApi(MailConfig mailConfig) {
        //     return MailgunClient.config(mailConfig.mailgun.privateApiKey)
        //         .createApi(MailgunEventsApi.class)
        // }
    }

}
