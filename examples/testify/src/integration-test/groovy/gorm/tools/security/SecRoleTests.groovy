package gorm.tools.security

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.security.Roles
import yakworks.security.gorm.model.SecRole
import yakworks.testing.gorm.integration.DataIntegrationTest
import yakworks.testing.gorm.integration.SecuritySpecHelper

@Integration
@Rollback
class SecRoleTests  extends Specification implements SecuritySpecHelper, DataIntegrationTest {

    void "update permissions"() {
        setup:
        SecRole cust = SecRole.query(code: Roles.CUSTOMER).get()
        List<String> perms = ["*:*:test"] + cust.permissions

        when:
        SecRole.repo.update([id:cust.id, permissions:perms])
        flushAndClear()

        then:
        noExceptionThrown()

        when:
        cust.refresh()

        then:
        cust.permissions.size() == perms.size()
        cust.hasPermission("*:*:test")
    }

    void "add remove permissions"() {
        setup:
        SecRole cust = SecRole.query(code: Roles.CUSTOMER).get()
        String perm = "*:*:test"
        expect:
        cust

        when:
        //save in a trx and load in different trx
        SecRole.withNewTransaction {
            cust.addPermission(perm)
            cust.persist()
        }
        flushAndClear()
        SecRole.withNewTransaction {
            cust = SecRole.get(cust.id)
        }

        then:
        cust.hasPermission(perm)

        when: "Remove permission"
        SecRole.withNewTransaction {
            cust.removePermission(perm)
            cust.persist()
        }
        flushAndClear()
        SecRole.withNewTransaction {
            cust = SecRole.get(cust.id)
        }

        then:
        !cust.hasPermission(perm)
    }
}
