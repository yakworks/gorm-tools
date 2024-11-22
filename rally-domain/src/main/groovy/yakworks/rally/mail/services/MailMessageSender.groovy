/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail.services

import java.time.LocalDateTime
import javax.inject.Inject

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import grails.gorm.transactions.Transactional
import yakworks.api.Result
import yakworks.api.problem.Problem
import yakworks.api.problem.ThrowableProblem
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.mail.EmailService
import yakworks.rally.mail.EmailService.MailTo
import yakworks.rally.mail.model.ContentType
import yakworks.rally.mail.model.MailMessage

import static yakworks.rally.mail.EmailUtils.validateEmail

/**
 * Sends a MailMessage domain and updates it with appropriate state and msgReponse
 * FUTURE: This will also be reponsible for updating a MailMessage when we capture mailgu events and want to update Delivered, Opened, Complained
 */
@Slf4j
@CompileStatic
class MailMessageSender {

    @Inject EmailService emailService

    /**
     * calls mailgunMessagesApi.sendMessage using the MailgunConfig.defaultDomain
     *
     */
    Result send(MailMessage mailMessage) {
        Result result

        try {
            MailTo mailTo = convertMailMessage(mailMessage)
            //validation throws a problem exception if validation fails
            validateMailMessage(mailTo)
            //mailService.send should never throw ex and should return result
            result = emailService.send(mailTo)

        } catch(ThrowableProblem p ) {
            result = p
        }  catch (Exception ex) {
            //in convertMailMessage might throw an ex if the attachmentId is bad or not found
            result = Problem.of(ex)
        }

        try {
            updateMessageState(mailMessage, result)
        } catch (pex) {
            //should not happen, unexpected
            log.error("Failed to update message state", pex)
            result = Problem.of(pex)
        }

        return result
    }

    /**
     * Validates mailto, throw DataProblem exception if validation fails
     */
    void validateMailMessage(MailTo mailTo) {
        validateEmail(mailTo.to.join(","))
        validateEmail(mailTo.from)
        if(mailTo.replyTo) validateEmail(mailTo.replyTo)
        if(mailTo.cc) validateEmail(mailTo.cc.join(","))
        if(mailTo.bcc) validateEmail(mailTo.bcc.join(","))
    }

    /**
     * updates and persists the mailMessage.state based on results.
     * Will assign the messageId and state=Sent on success
     */
    @Transactional
    protected void updateMessageState(MailMessage mailMessage, Result result) {
        if (result instanceof Problem) {
            mailMessage.state = MailMessage.MsgState.Failed
            mailMessage.msgResponse = result.detail
        } else {
            Map payload = (Map) result.payload
            mailMessage.messageId = payload.id
            mailMessage.sendDate = LocalDateTime.now()
            mailMessage.state = MailMessage.MsgState.Sent
        }
        mailMessage.persist()
    }

    MailTo convertMailMessage(MailMessage mailMessage) {
        MailTo mailTo = new MailTo(
            from: mailMessage.sendFrom,
            replyTo: mailMessage.replyTo,
            to: [mailMessage.sendTo],
            subject: mailMessage.subject
        )

        //TODO only html, plain handled, need to impl markdown
        if (mailMessage.contentType == ContentType.html) {
            mailTo.html = mailMessage.body
        } else {
            mailTo.text = mailMessage.body
        }

        if (mailMessage.cc) mailTo.cc = [mailMessage.cc]
        if (mailMessage.bcc) mailTo.cc = [mailMessage.bcc]

        if (mailMessage.attachmentIds) {
            List attachFiles = [] as List<File>
            mailMessage.attachmentIds.each { Long id ->
                Attachment attach = Attachment.get(id)
                if (!attach) throw new IllegalArgumentException("Attachment id:[$id] does not exist")
                attachFiles.add(attach.resource.getFile())
            }
            if (attachFiles) mailTo.attachments = attachFiles
        }
        //tags
        if (mailMessage.tags) mailTo.tags = mailMessage.tags
        //TODO do inline
        return mailTo
    }

}
