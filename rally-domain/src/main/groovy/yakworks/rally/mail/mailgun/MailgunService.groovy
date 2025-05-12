/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail.mailgun

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.dao.OptimisticLockingFailureException

import com.mailgun.api.v3.MailgunEventsApi
import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.client.MailgunClient
import com.mailgun.model.events.EventsQueryOptions
import com.mailgun.model.events.EventsResponse
import com.mailgun.model.message.Message
import com.mailgun.model.message.MessageResponse
import com.mailgun.util.ObjectMapperUtil
import feign.FeignException
import yakworks.api.Result
import yakworks.api.problem.Problem
import yakworks.api.problem.data.DataProblem
import yakworks.rally.mail.EmailService

/**
 * Basic service for MailGun.
 * works for a basic send, recomended to use the mailgunMessagesApi for anything outside of a simple send
 * and use the mailgunEventsApi for filtering  anything outside of a basic event calls, here for baseline examples of what can be done
 */
@Slf4j
@CompileStatic
class MailgunService extends EmailService {

    private MailgunMessagesApi _mailgunMessagesApi
    private MailgunEventsApi _mailgunEventsApi

    Message mailMsgToMessage(MailTo mailMsg){
        Message.MessageBuilder bldr = Message.builder()
        bldr
            .from(mailMsg.from)
            .replyTo(mailMsg.replyTo)
            .to(mailMsg.to)
            .subject(mailMsg.subject)

        if(mailMsg.cc) bldr.cc(mailMsg.cc)
        if(mailMsg.bcc) bldr.bcc(mailMsg.bcc)
        if(mailMsg.tags) bldr.tag(mailMsg.tags)
        if(mailMsg.text) bldr.text(mailMsg.text)
        if(mailMsg.html) bldr.html(mailMsg.html) //html will be present, when contentType=html
        if(mailMsg.attachments) bldr.attachment(mailMsg.attachments as List<File>)

        return bldr.build()
    }

    /**
     * Will try to send mail through mailgun api, and retry twice before giving up.
     * With 3 seconds pause between retries
     */
    MessageResponse sendWithRetry(String domain, Message message) {
        int attempts = 0
        boolean success = false
        int maxTries = 2
        int retryAfterSeconds = 3

        while (!success && attempts < maxTries) {
            try {
                MessageResponse resp = sendMessage(domain, message)
                success = true
                return resp
            } catch (FeignException e) {
                //FeignException would happen when mailgun api call fails, eg 500 API Error
                attempts++
                log.warn("Mailgun API call failed, attempt:$attempts, to:${message.to}, subject:${message.subject}")
                //if 2 attempts are over, or if we receive 401 Unauthorized, thn dont attempt again.
                if (attempts == maxTries || e.status() == 401) {
                    throw e
                } else {
                    //sleep before retrying again
                    sleep(retryAfterSeconds * 1000)
                }
            }
        }
    }


    /**
     * calls mailgunMessagesApi.sendMessage
     * The return Result has a payload Map with id and message
     * @param domain the mailgun domain name
     * @param mailTo the MailTo message to send.
     * @return The result with payload
     */
    @Override
    Result send(String domain, MailTo mailTo){
        try{
            Message message = mailMsgToMessage(mailTo)
            MessageResponse resp = sendWithRetry(domain, message)
            log.debug("Mail gun response : domain:$domain, id:${resp.id}, message:${resp.message}")
            return Result.OK().payload([id: resp.id, message: resp.message])
        } catch(FeignException e){
            //FeignException would happen when mailgun api call fails
            log.error("Mailgun failed to send email [${e.message}] \n ${mailTo}")
            if(e.status() == 401) return new DataProblem().title("Unauthorized or bad domain").status(e.status())
            Map msgData =  ObjectMapperUtil.getObjectMapper().readValue(e.contentUTF8(), Map)
            return new DataProblem().title("Mailgun Send Failure").detail(msgData['message'] as String).status(e.status())
        } catch(ex){
            //catch anything else, and log to help investigate
            log.error("Mailgun Send Failure [${ex.message}] \n ${mailTo}")
            return Problem.of(ex)
        }

    }

    /**
     * calls mailgunMessagesApi.sendMessage
     */
    MessageResponse sendMessage(String domain, Message message){
        MessageResponse resp = mailgunMessagesApi.sendMessage(domain, message)
        return resp
    }


    EventsResponse getEvents(String domain, EventsQueryOptions queryOptions){
        return mailgunEventsApi.getEvents(domain, queryOptions)
    }

    EventsResponse getEvents(EventsQueryOptions queryOptions = null){
        if(queryOptions){
            return mailgunEventsApi.getEvents(mailProps.defaultDomain, queryOptions)
        } else {
            return mailgunEventsApi.getAllEvents(mailProps.defaultDomain);
        }
    }

    MailgunMessagesApi getMailgunMessagesApi() {
        if(!_mailgunMessagesApi) {
            _mailgunMessagesApi = MailgunClient.config(mailProps.mailgun.privateApiKey)
                .createApi(MailgunMessagesApi.class)
        }
        return _mailgunMessagesApi
    }

    MailgunEventsApi getMailgunEventsApi() {
        if(!_mailgunEventsApi) {
            _mailgunEventsApi = MailgunClient.config(mailProps.mailgun.privateApiKey)
                .createApi(MailgunEventsApi.class)
        }
        return _mailgunEventsApi
    }
}
