/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

import yakworks.rally.mail.mailgun.MailgunService

@Configuration @Lazy
@ConfigurationPropertiesScan([ "yakworks.rally.mail.config" ])
//@ComponentScan(['nine.mailgun']) //scan for the Repos, anything other beans should be specified here
@CompileStatic
class MailSpringConfig {

    //@Autowired MailConfig mailConfig

    @Configuration @Lazy
    static class MailgunBeans {

        @Bean
        MailgunService mailService() {
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
