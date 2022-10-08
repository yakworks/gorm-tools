package yakity.security

import groovy.util.logging.Slf4j

import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.stereotype.Component

@Slf4j
@Component
class AuthenticationEvents {

    @EventListener
    void onSuccess(AuthenticationSuccessEvent success) {
        log.trace("😀😀😀😀😀😀😀😀 SUCCESS ${success.authentication.class.name}", success.authentication)
    }

    @EventListener
    void onFailure(AbstractAuthenticationFailureEvent failures) {
        log.trace("👹👹👹👹👹👹👹👹👹FAILURE ${failures.class.name}", failures.exception)
    }
}
