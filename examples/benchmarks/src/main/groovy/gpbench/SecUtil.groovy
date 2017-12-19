package gpbench

import gorm.tools.beans.AppCtx
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.userdetails.GrailsUser
import groovy.transform.CompileStatic

@CompileStatic
class SecUtil {

    static Long getUserId() {
        def secServ = AppCtx.get("springSecurityService", SpringSecurityService)
        ((GrailsUser) secServ.principal).id as Long
    }
}
