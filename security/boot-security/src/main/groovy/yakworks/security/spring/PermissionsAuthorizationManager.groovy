/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import java.util.function.Supplier
import javax.servlet.http.HttpServletRequest

import org.apache.shiro.authz.permission.WildcardPermission
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.access.intercept.RequestAuthorizationContext

/**
 * AuthorizationManager that uses Shiro permissions to check the URL path against Wilcard permissions
 */
class PermissionsAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Value('${app.security.permissions.enabled:true}')
    boolean permissionsEnabled

    @Value('${app.security.enabled:true}')
    boolean securityEnabled

    //will intercept urls under this paths
    List<String> contextPaths = ['api', 'jobs']

    AuthenticationTrustResolver authenticationTrustResolver

    PermissionsAuthorizationManager() {
        this(new AuthenticationTrustResolverImpl())
    }

    PermissionsAuthorizationManager(AuthenticationTrustResolver tr) {
        this.authenticationTrustResolver = tr
    }

    @Override
    AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get()
        HttpServletRequest request = context.getRequest()

        //if its not enabled then always return true
        if(!securityEnabled || !permissionsEnabled){
            return new AuthorizationDecision(true)
        }

        //just disallow if its unauthenticated or anonymous authentication
        if (!authentication || !authentication.isAuthenticated() || authenticationTrustResolver.isAnonymous(authentication)) {
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
        if (segments.size() >= 2 && (segments[0] in contextPaths)) {

            //if its a put or delete request, and last part is number or UUID, eg PUT /api/ar/tran/1
            //then remove the last part (id) to build a permission like ar:tran:read, isntead of ar:tran:1:read
            //FIXME hack for now but we should really base this on what is being done now to do the URLMapping.
            // then we can just use the controller and action
            String lastSegment = segments[-1]
            if(op in ['update', 'delete', 'read'] && (lastSegment.isNumber() || isUUID(lastSegment)) ) {
                segments.removeLast() //removes last item (id)
            }
            segments << op
            //join all parts except api, so it becomes "autocash:payment:read" for GET /api/autocash/payment
            return segments[1..-1].join(":")
        } else {
            return null
        }
    }

    protected boolean isUUID(String uidStr) {
        //def uidStr = '148f5327-b297-44bc-8ccc-46f8fd4c32e6'
        def matcher = uidStr =~ /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/
        return matcher.matches()
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
