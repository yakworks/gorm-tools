package yakity.security

import java.util.function.Supplier
import javax.servlet.http.HttpServletRequest

import org.grails.io.support.AntPathMatcher
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.access.intercept.RequestAuthorizationContext

class WildcardAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final AntPathMatcher pathMatcher = new AntPathMatcher()

    @Override
    AuthorizationDecision check(Supplier<Authentication> authenticationSupplier,
                                RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get()
        HttpServletRequest request = context.getRequest()

        if (!authentication?.isAuthenticated()) {
            return new AuthorizationDecision(false)
        }

        // Map request -> required permission
        String requiredPermission = mapToPermission(request)

        if (!requiredPermission) {
            // If no mapping found → deny by default
            return new AuthorizationDecision(false)
        }

        // User’s authorities as strings
        Set<String> userAuthorities = authentication.authorities*.authority as Set<String>

        // Support wildcards: e.g., "ar:autocash:*"
        boolean granted = userAuthorities.any { userAuth ->
            wildcardMatch(userAuth, requiredPermission)
        }

        return new AuthorizationDecision(granted)
    }

    private boolean wildcardMatch(String pattern, String value) {
        pathMatcher.match(pattern, value)
    }

    String mapToPermission(HttpServletRequest request) {
        String method = request.method
        String path = request.requestURI

        // Normalize: /api/ar/autocash/123 → ar:autocash:read
        def segments = path.tokenize('/')
        if (segments.size() >= 2 && segments[0] == 'api') {

            String action
            switch (method) {
                case "GET": action = "read"; break
                case "POST": action = "create"; break
                case "PUT": action = "update"; break
                case "DELETE": action = "delete"; break
                default: return null
            }

            segments << action

            //join all parts except api, so it becomes "ar:autocash:payment:read" for GET /api/ar/autocash/payment
            return segments[1..-1].join(":")
        }
        return null
    }
}
