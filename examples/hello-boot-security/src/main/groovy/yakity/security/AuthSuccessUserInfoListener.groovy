package yakity.security

import groovy.util.logging.Slf4j

import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.stereotype.Component

/**
 * Listener to update details in the Authentication with our UserInfo facade.
 * By storing it in details instead of principal trying to mess around with the principal we only have one place to do it.
 * The only thing stored in details by default is the ip adresss and sessionId which we just proxy in the SpringUser.
 * Also makes it easy to override for customer funtionanlity by simply implementing you own listener.
 */
@Slf4j
@Component
class AuthSuccessUserInfoListener {

    @EventListener
    void onSuccess(AuthenticationSuccessEvent success) {
        log.trace("😀😀😀😀😀😀😀😀 SUCCESS ${success.authentication.class.name}", success.authentication)
        //if its already a UserInfo then just set it.

    }

}
