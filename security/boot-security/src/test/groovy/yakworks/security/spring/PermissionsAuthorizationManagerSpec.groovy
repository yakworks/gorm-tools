package yakworks.security.spring

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import spock.lang.Specification

import java.util.function.Supplier
import javax.servlet.http.HttpServletRequest

class PermissionsAuthorizationManagerSpec extends Specification {

    PermissionsAuthorizationManager manager = new PermissionsAuthorizationManager()

    void "test mapToPermission crud"() {
        expect:
        manager.mapToPermission(mockRequest("GET", "/api/rally/org")) == "rally:org:read"
        manager.mapToPermission(mockRequest("GET", "/api/rally/org/1")) == "rally:org:read"

        manager.mapToPermission(mockRequest("POST", "/api/rally/org")) == "rally:org:create"

        manager.mapToPermission(mockRequest("PUT", "/api/rally/org")) == "rally:org:update"
        manager.mapToPermission(mockRequest("PUT", "/api/rally/org/1")) == "rally:org:update"

        manager.mapToPermission(mockRequest("DELETE", "/api/rally/org/1")) == "rally:org:delete"
        manager.mapToPermission(mockRequest("DELETE", "/api/rally/org")) == "rally:org:delete"
    }

    void "jobs"() {
        manager.mapToPermission(mockRequest("POST", "/jobs/job/trigger")) == "job:trigger:create"
    }

    void "bulk permissions"() {
        manager.mapToPermission(mockRequest("GET", "/api/rally/org/bulk")) == "rally:org:bulk:read"
        manager.mapToPermission(mockRequest("PUT", "/api/rally/org/bulk")) == "rally:org:bulk:update"
    }

    void "massUpdate maps to update permission"() {
        expect:
        manager.mapToPermission(mockRequest("PUT", "/api/rally/org/massUpdate")) == "rally:org:update"
        manager.mapToPermission(mockRequest("PUT", "/api/ar/tran/massUpdate")) == "ar:tran:update"
    }

    void "massUpdate is allowed when update is allowed"() {
        setup:
        manager.securityEnabled = true
        manager.permissionsEnabled = true
        HttpServletRequest request = mockRequest("PUT", "/api/rally/org/massUpdate")

        expect:
        manager.check(authWith(perm), new RequestAuthorizationContext(request)).isGranted() == granted

        where:
        perm                    | granted
        "rally:org:update"      | true
        "rally:org:*"           | true
        "rally:*:update"        | true
        "*:*:*"                 | true
        "rally:org:read,create" | false
        "*:*:read"              | false
        "rally:contact:update"  | false
    }

    void "test mapToPermission rpc"() {
        expect:
        manager.mapToPermission(mockRequest("GET", "/api/rally/org/rpc", [op:"rpc1"])) == "rally:org:rpc:rpc1"
    }

    void "test isUUID"() {
        expect:
        !manager.isUUID('foo')
        !manager.isUUID('123')
        !manager.isUUID('1edca10c-0e0f-67e9')
        manager.isUUID('1edca10c-0e0f-67e9-b1f6-757c83c39281')
        manager.isUUID(UUID.randomUUID().toString() )
    }

    Supplier<Authentication> authWith(String... perms) {
        List authorities = perms.collect { new SimpleGrantedAuthority(it) }
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "pwd", authorities)
        return { auth } as Supplier<Authentication>
    }

    HttpServletRequest mockRequest(String method, String path, Map params = null) {
        MockHttpServletRequest request = new MockHttpServletRequest()
        request.setMethod(method)
        request.setRequestURI(path)
        if(params) {
            request.setParameters(params)
        }
        return request
    }

}
