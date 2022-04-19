package yakworks.security

import java.time.LocalDateTime

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Value

import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecRoleUser
import gorm.tools.security.services.SecService
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import yakworks.rally.orgs.model.Contact

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

    // List<AppUser> getBranchManagers(Org branch) {
    //     Validate.notNull(branch, "Branch is null")
    //     if (branch.orgTypeId != OrgType.BRANCH) {
    //         throw new IllegalArgumentException("Not a branch $branch")
    //     }
    //     getOrgManagers(branch.id)
    // }

    // @Deprecated
    // AppUser getManager(Org org) {
    //     if (!org) throw new IllegalArgumentException("Org is null")
    //     List<SecRole> roles = SecRole.findAllByNameInList([ROLE_MANAGER, ROLE_COLLECTIONS_MANAGER])
    //
    //     SecRoleUser sru = SecRoleUser.find("")
    //
    //     return (AppUser)sru?.user
    // }

    /**
     * List of managers for specified organization
     */
    @ReadOnly
    List<AppUser> getOrgManagers(Long orgId) {
        DetachedCriteria secSubQuery = SecRoleUser.query {
            setAlias 'secRoleUser'
            createAlias('role', 'role')
            eqProperty("user.id", "appUser.id")
            inList('role.name', [Roles.COLLECTIONS_MANAGER, Roles.MANAGER])
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
            user.id = contact.id
            contact.user = user
            user.persist()
        }
        return contact.user
    }
}
