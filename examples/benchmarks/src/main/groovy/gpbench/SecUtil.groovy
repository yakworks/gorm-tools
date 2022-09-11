package gpbench

import groovy.transform.CompileStatic

import grails.plugin.springsecurity.SpringSecurityService
import yakworks.security.spring.SpringSecUser
import yakworks.spring.AppCtx

@CompileStatic
class SecUtil {

    static Long getUserId() {
        def secServ = AppCtx.get("springSecurityService", SpringSecurityService)
        ((SpringSecUser) secServ.principal).id as Long
    }
}
