package gpbench

import groovy.transform.CompileStatic

import grails.plugin.springsecurity.SpringSecurityService
import yakworks.security.spring.user.SpringUser
import yakworks.spring.AppCtx

@CompileStatic
class SecUtil {

    static Long getUserId() {
        def secServ = AppCtx.get("springSecurityService", SpringSecurityService)
        ((SpringUser) secServ.principal).id as Long
    }
}
