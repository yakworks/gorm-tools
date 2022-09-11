package gpbench

import groovy.transform.CompileStatic

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.userdetails.GrailsUser
import yakworks.spring.AppCtx

@CompileStatic
class SecUtil {

    static Long getUserId() {
        def secServ = AppCtx.get("springSecurityService", SpringSecurityService)
        ((GrailsUser) secServ.principal).id as Long
    }
}
