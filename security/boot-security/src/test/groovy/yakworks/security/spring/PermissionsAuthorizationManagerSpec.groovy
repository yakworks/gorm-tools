package yakworks.security.spring

import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

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
