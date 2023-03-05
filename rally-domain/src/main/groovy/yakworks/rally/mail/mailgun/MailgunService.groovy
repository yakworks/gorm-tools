/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail.mailgun

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

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
import yakworks.rally.mail.MailService
import yakworks.rally.mail.MailTo

/**
 * Basic service for MailGun.
 * works for a basic send, recomended to use the mailgunMessagesApi for anything outside of a simple send
 * and use the mailgunEventsApi for filtering  anything outside of a basic event calls, here for baseline examples of what can be done
 */
@Slf4j
@CompileStatic
class MailgunService extends MailService {

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
        if(mailMsg.html) bldr.html(mailMsg.html)
        if(mailMsg.attachments) bldr.attachment(mailMsg.attachments as List<File>)

        return bldr.build()
    }

    /**
     * calls mailgunMessagesApi.sendMessage
     */
    @Override
    Result send(String domain, MailTo mailMsg){
        try{
            Message message = mailMsgToMessage(mailMsg)
            MessageResponse resp = sendMessage(domain, message)
            return Result.OK().payload(resp)
        } catch(FeignException e){
            if(e.status() == 401) return new DataProblem().title("Unauthorized or bad domain").status(e.status())
            Map msgData =  ObjectMapperUtil.getObjectMapper().readValue(e.contentUTF8(), Map)
            return new DataProblem().title("Mailgun Send Failure").detail(msgData['message'] as String).status(e.status())
        } catch(ex){
            //return Problem.of('error.illegalArgument').status(status400).detail(e.message)
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
            return mailgunEventsApi.getEvents(mailConfig.defaultDomain, queryOptions)
        } else {
            return mailgunEventsApi.getAllEvents(mailConfig.defaultDomain);
        }
    }

    MailgunMessagesApi getMailgunMessagesApi() {
        if(!_mailgunMessagesApi) {
            _mailgunMessagesApi = MailgunClient.config(mailConfig.mailgun.privateApiKey)
                .createApi(MailgunMessagesApi.class)
        }
        return _mailgunMessagesApi
    }

    MailgunEventsApi getMailgunEventsApi() {
        if(!_mailgunEventsApi) {
            _mailgunEventsApi = MailgunClient.config(mailConfig.mailgun.privateApiKey)
                .createApi(MailgunEventsApi.class)
        }
        return _mailgunEventsApi
    }
}
