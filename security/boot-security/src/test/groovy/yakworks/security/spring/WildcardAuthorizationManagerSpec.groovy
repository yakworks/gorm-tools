package yakworks.security.spring

import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class WildcardAuthorizationManagerSpec extends Specification {

    void "test mapToPermission crud"() {
        setup:
        WildcardAuthorizationManager manager = new WildcardAuthorizationManager()

        expect:
        manager.mapToPermission(mockRequest("GET", "/api/rally/org")) == "rally:org:read"
        manager.mapToPermission(mockRequest("POST", "/api/rally/org")) == "rally:org:create"
        manager.mapToPermission(mockRequest("PUT", "/api/rally/org/1")) == "rally:org:1:update"
        manager.mapToPermission(mockRequest("DELETE", "/api/rally/org/1")) == "rally:org:1:delete"
    }

    void "test mapToPermission rpc"() {
        setup:
        WildcardAuthorizationManager manager = new WildcardAuthorizationManager()

        expect:
        manager.mapToPermission(mockRequest("GET", "/api/rally/org/rpc", [op:"rpc1"])) == "rally:org:rpc:rpc1"
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
