package yakworks.security

import gorm.tools.repository.model.DataOp
import grails.testing.mixin.integration.Integration
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import spock.lang.Specification
import yakworks.rally.api.OrgCrudApi
import yakworks.rally.orgs.model.OrgType
import yakworks.rest.gorm.SecureCrudApi

import javax.inject.Inject

@Integration
class SecureCrudApiSpec extends Specification {

    @Inject OrgCrudApi orgCrudApi
    @Inject SecService service

    void "sanity check"() {
        expect:
        orgCrudApi
        orgCrudApi instanceof SecureCrudApi
    }

    void "not logged in"() {
        when:
        orgCrudApi.create(orgData, [:])

        then:
        AuthenticationException ex = thrown()

        when:
        orgCrudApi.update(orgData, [:])

        then:
        ex = thrown()

        when:
        orgCrudApi.upsert(orgData, [:])

        then:
        ex = thrown()

        when:
        orgCrudApi.removeById(1L, [:])

        then:
        ex = thrown()

        when:
        orgCrudApi.bulk(DataOp.update, [orgData], [:], "Test")

        then:
        ex = thrown()
    }

    void "readonly user"() {
        setup:
        service.login("readonly", "123")

        when:
        orgCrudApi.create(orgData, [:])

        then:
        AccessDeniedException ex = thrown()
        ex.message == 'Access Denied'

        when:
        orgCrudApi.update(orgData, [:])

        then:
        ex = thrown()
        ex.message == 'Access Denied'

        when:
        orgCrudApi.upsert(orgData, [:])

        then:
        ex = thrown()
        ex.message == 'Access Denied'

        when:
        orgCrudApi.removeById(1L, [:])

        then:
        ex = thrown()
        ex.message == 'Access Denied'

        when:
        orgCrudApi.bulk(DataOp.update, [orgData], [:], "Test")

        then:
        ex = thrown()
        ex.message == 'Access Denied'
    }

    Map getOrgData() {
        return [num:"T1", name:"T1", type: OrgType.Customer.name()]
    }

}
