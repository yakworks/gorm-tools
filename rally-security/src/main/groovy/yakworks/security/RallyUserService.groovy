/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import java.time.LocalDateTime

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Value

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import yakworks.rally.orgs.model.Contact
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRoleUser

// import grails.plugin.freemarker.FreeMarkerViewService

/**
 * UserService is for user level helpers, such as sending emails to user,
 * tracking user login/logout And operations relating to passwords, contacts and org levels
 */
@Slf4j
@CompileStatic
class RallyUserService {

    SecService secService
    // def freeMarkerViewService
    // EmailerService emailerService

    @Value('${grails.serverURL:http://localhost:8080}')
    String serverUrl

    final static String RESET_PASSWORD_SUBJECT = "Greenbill password reset"
    final static String RESET_PASSWORD_TEMPLATE = "/nineLogin/resetPasswordEmail.ftl"
    final static String RESET_PASSWORD_ACK = "/nineLogin/resetPasswordAck.ftl"

    @CompileDynamic //TODO refactor out to its own messaging area
    //dynamic so we don't need to depend on FreeMarkerViewService, see notes in build.gradle, can remove once sorted out
    // void sendResetPasswordLink(AppUser user, String token) {
    //     Map model = ['name': user.name, 'userName': user.username, 'appUrl': serverUrl, "token": token]
    //     Writer writer = freeMarkerViewService.render(RESET_PASSWORD_TEMPLATE, model, new StringBuilderWriter())
    //     emailerService.sendMail(user.email, RESET_PASSWORD_SUBJECT, writer.toString())
    // }

    @CompileDynamic //TODO refactor out to its own messaging area
    // void sendResetPasswordAck(AppUser user, String password) {
    //     Map model = ['name': user.name, 'userName': user.username, password: password]
    //     Writer writer = freeMarkerViewService.render(RESET_PASSWORD_ACK, model, new StringBuilderWriter())
    //     emailerService.sendMail(user.email, "Greenbill New Password", writer.toString())
    // }

    @Transactional
    String generateResetPasswordToken(AppUser user) {
        String token = UUID.randomUUID().toString().replaceAll('-', '')
        user.resetPasswordToken = token
        user.resetPasswordDate = LocalDateTime.now()
        user.persist(flush: true)
        return token
    }

    /**
     * List of managers for specified organization
     */
    @ReadOnly
    List<AppUser> getOrgManagers(Long orgId) {
        DetachedCriteria secSubQuery = SecRoleUser.query {
            setAlias 'secRoleUser'
            createAlias('role', 'role')
            eqProperty("user.id", "appUser.id")
            inList('role.name', [Roles.AR_MANAGER, Roles.MANAGER])
        }
        DetachedCriteria conSubQuery = Contact.query {
            setAlias 'contact'
            eqProperty("id", "appUser.id")
            eq 'org.id', orgId
        }
        DetachedCriteria userCrit = AppUser.query {
            setAlias 'appUser'
            exists secSubQuery.id()
            exists conSubQuery.id()
        }

        return userCrit.list()
    }


    /*
    * build a User domain object from a contact if it does not exist.
    */
    @Transactional
    AppUser buildUserFromContact(Contact contact, String password) {
        if (!contact.user) {
            AppUser user = new AppUser(username: contact.email, email: contact.email)
            user.password = password //plain text password gets encrypted on persist to passHash
            user.id = contact.getId()
            contact.user = user
            user.persist()
        }
        return contact.user
    }
}
