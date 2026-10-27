package gorm.tools.security

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.security.gorm.model.AppUser
import yakworks.security.user.CurrentUser
import yakworks.testing.gorm.integration.DataIntegrationTest
import yakworks.testing.gorm.integration.SecuritySpecHelper

@Integration
@Rollback
class SecuritySpecHelperSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    CurrentUser currentUser

    void "default user is admin"() {
        expect:
        currentUser
        currentUser.user
        currentUser.user.id == 1
        currentUser.user.roles
        currentUser.user.permissions
        currentUser.hasRole('ADMIN')
        currentUser.hasPermission('test:test:test')
    }

    void "authenticate specified user with default roles and permissions"() {
        when:
        authenticate(AppUser.get(2L))

        then:
        currentUser
        currentUser.user
        currentUser.user.id == 2
        currentUser.hasRole('CUSTOMER')

        and:
        currentUser.user.permissions
        currentUser.hasPermission('rally:activity:list')
    }

    void "authenticate user with provided roles and permissions"() {
        when:
        authenticate(AppUser.get(2L), ['CUST'], ['test:test'])

        then:
        currentUser
        currentUser.user
        currentUser.user.id == 2
        !currentUser.hasRole('CUSTOMER')
        currentUser.hasRole('CUST')

        and:
        currentUser.user.permissions
        !currentUser.hasPermission('rally:activity:list')
        currentUser.hasPermission('test:test')
    }
}
