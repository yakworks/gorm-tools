/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail

import javax.inject.Inject

import groovy.transform.CompileStatic
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

    @Inject MailProps mailConfig

    /**
     * calls mailgunMessagesApi.sendMessage
     */
    abstract Result send(String domain, MailTo mailMsg)

    /**
     * calls mailgunMessagesApi.sendMessage using the MailgunConfig.defaultDomain
     */
    Result send(MailTo mailMsg){
        return send(mailConfig.defaultDomain, mailMsg)
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

}
