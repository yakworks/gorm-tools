/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail.services

import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.problem.ProblemHandler
import grails.gorm.transactions.Transactional
import yakworks.api.Result
import yakworks.handlebars.Bars
import yakworks.rally.activity.ActivityService
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.mail.model.ContentType
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.mail.model.MailerTemplate

/**
 * Service for MailerTemplates with handlebars.
 */
@Slf4j
@CompileStatic
class CommonMailer {

    @Autowired ActivityRepo activityRepo
    @Autowired ActivityService activityService
    @Autowired ProblemHandler problemHandler

    /**
     * Creates the MailMessage and saves it.
     * @param sendTo who to send to, can be comma separated list
     * @param mailerTemplate the mailer template, can come from config
     * @param model the model, just like an other for gsp, jsp, etc.. this is what the handlebars tempalte will use
     * @return the saved MailMessage
     */
    @Transactional
    MailMessage createMailMessage(String sendTo, MailerTemplate mailerTemplate, Map<String, Object> model){

        assert mailerTemplate.subject
        String subject = applyHandlebars(mailerTemplate.subject, model)

        String body = applyHandlebarsBody(mailerTemplate, model)

        //two extra blank lines at the bottom, if plain text email
        if(mailerTemplate.contentType == ContentType.plain) {
            body = body + "\n\n"
        }

        MailMessage msg = new MailMessage(
            state: MailMessage.MsgState.Queued,
            sendTo: sendTo,
            sendFrom: mailerTemplate.sendFrom,
            replyTo: mailerTemplate.replyTo,
            subject: subject,
            tags: mailerTemplate.tags,
            body: body,
            contentType: mailerTemplate.contentType
        )
        if(mailerTemplate.attachmentIds)  msg.attachmentIds = mailerTemplate.attachmentIds
        msg.persist()
        return msg
    }

    /**
     * helper method that redirects to the activityService.buildEmail
     *
     * @param orgId the Org the act is for
     * @param mailMessage the mailMessage to attach to act
     * @return an unsaved Activity
     */
    Activity buildEmailActivity(Long orgId, MailMessage mailMessage){
        activityService.buildEmail(orgId, mailMessage)
    }

    /**
     * Sends the email catching any errors into a problem, so this never throws
     * @param activityId the id to send.
     * @return the Result or Problem
     */
    Result sendEmail(Long activityId){
        try{
            return activityService.sendEmail(activityId)
        } catch(ex){
            return problemHandler.handleException(ex)
        }
    }

    String applyHandlebars(String templ, Map model){
        return Bars.applyInline(templ, model)
    }

    String applyHandlebarsBody(MailerTemplate templateProps, Map model){
        assert templateProps.body || templateProps.bodyTemplate?.exists()
        if(templateProps.body){
            return Bars.applyInline(templateProps.body, model)
        } else { //if no body then its expected to have a bodyTemplate
            Path origFile = templateProps.bodyTemplate
            assert origFile.exists()
            String tpl = origFile.text
            return Bars.applyInline(tpl, model)
        }
    }

}
