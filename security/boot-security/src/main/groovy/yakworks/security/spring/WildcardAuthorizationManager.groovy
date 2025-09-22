/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import java.util.function.Supplier
import javax.servlet.http.HttpServletRequest

import org.apache.shiro.authz.permission.WildcardPermission
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.access.intercept.RequestAuthorizationContext

class WildcardAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
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

        WildcardPermission requiredWildcardPerm = toWildcardPermission(requiredPermission)

        // User’s authorities as strings
        Set<String> userAuthorities = authentication.authorities*.authority as Set<String>

        boolean granted = userAuthorities.any { userAuth ->
            toWildcardPermission(userAuth).implies(requiredWildcardPerm)
        }

        return new AuthorizationDecision(granted)
    }

    /**
     * Maps request to a required permission based on request path
     */
    String mapToPermission(HttpServletRequest request) {
        String method = request.method
        String path = request.requestURI

        String op = null

        if(isRpc(path)) {
            op = request.getParameter('op')
        } else {
            switch (method) {
                case "GET": op = "read"; break
                case "POST": op = "create"; break
                case "PUT": op = "update"; break
                case "DELETE": op = "delete"; break
                default: return null
            }
        }

       return buildPermission(path, op)
    }

    /**
     * Normalize: /api/ar/autocash/123 → ar:autocash:read
     * api/ar/tran/rpc?op=reverse -> ar:tran:rpc:reverse
     */
    //FIXME - Cache
    protected String buildPermission(String path, String op) {
        List<String> segments = path.tokenize('/')
        if (segments.size() >= 2 && segments[0] == 'api') {

            //if its a put or delete request, and last part is number, eg PUT /api/ar/tran/1
            //then remove the last part (id) to build a permission like ar:tran:read, isntead of ar:tran:1:read
            if(op in ['update', 'delete'] && segments[-1].isNumber()) {
                segments.removeAt(segments.size() - 1) //removes last item
            }
            segments << op
            //join all parts except api, so it becomes "autocash:payment:read" for GET /api/autocash/payment
            return segments[1..-1].join(":")
        } else {
            return null
        }
    }

    /**
     * Check if request is a RPC request
     */
    //FIXME - Cache
    protected boolean isRpc(String path) {
        return path.endsWith("/rpc")
    }

    protected WildcardPermission toWildcardPermission(String perm) {
        return new WildcardPermission(perm)
    }
}
