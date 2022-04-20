package yakworks.security.shiro

import org.springframework.stereotype.Component
import org.apache.shiro.authz.annotation.Logical
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresGuest
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.apache.shiro.authz.annotation.RequiresRoles
import org.apache.shiro.authz.annotation.RequiresUser
import grails.plugin.springsecurity.annotation.Secured

@Component
class TestService {

    @RequiresPermissions('printer:maintain:epsoncolor')
    boolean adminPrinter() { true }

    @RequiresPermissions(['action:jump', 'action:kick'])
    boolean requireJumpAndKick() { true }

    @RequiresPermissions(value=['action:jump', 'action:kick'], logical=Logical.OR)
    boolean requireJumpOrKick() { true }

    @RequiresPermissions('doesnt:exist')
    boolean impossiblePermissions() { true }

    @RequiresUser
    boolean requireUser() { true }

    @Secured('ADMIN')
    boolean requireAdminSpringSecured() { true }

    @RequiresRoles(value=['ADMIN', 'CUST'], logical=Logical.OR)
    boolean requireUserOrAdmin() { true }

    @RequiresRoles(value=['ADMIN', 'CUST'])
    boolean requireUserAndAdmin() { true }

    @RequiresGuest
    boolean requireGuest() { true }

    @RequiresAuthentication
    boolean requireAuthentication() { true }

    @RequiresPermissions('printer:admin')
    boolean requirePrinterAdminPermissions() { true }

    @RequiresPermissions('printer:use')
    boolean requireUsePrinterPermissions() { true }
}
