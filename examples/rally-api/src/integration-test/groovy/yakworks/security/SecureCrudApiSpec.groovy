package yakworks.security

import gorm.tools.repository.model.DataOp
import gorm.tools.utils.ServiceLookup
import grails.testing.mixin.integration.Integration
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import spock.lang.Specification
import yakworks.gorm.api.DefaultCrudApi
import yakworks.rally.api.OrgCrudApi
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rest.gorm.SecureCrudApi
import yakworks.gorm.api.CrudApi
import yakworks.spring.AppCtx

import javax.inject.Inject

@Integration
class SecureCrudApiSpec extends Specification {

    @Inject OrgCrudApi orgCrudApi
    @Inject SecService service

    SecureCrudApi<Org> orgSecureCrudApi

    void setup() {
        orgSecureCrudApi = AppCtx.ctx.getBean("secureCrudApi", [Org] as Object[])
    }

    void "sanity check"() {
        expect:
        orgCrudApi
        orgCrudApi instanceof DefaultCrudApi
        orgSecureCrudApi
        orgSecureCrudApi instanceof SecureCrudApi
    }

    void "not logged in"() {
        when:
        orgSecureCrudApi.create(orgData, [:])

        then:
        AuthenticationException ex = thrown()

        when:
        orgSecureCrudApi.update(orgData, [:])

        then:
        ex = thrown()

        when:
        orgSecureCrudApi.upsert(orgData, [:])

        then:
        ex = thrown()

        when:
        orgSecureCrudApi.removeById(1L, [:])

        then:
        ex = thrown()

        when:
        orgSecureCrudApi.bulkImport(DataOp.update, [orgData], [:], "Test")

        then:
        ex = thrown()
    }

    void "readonly user"() {
        setup:
        service.login("readonly", "123")

        when:
        orgSecureCrudApi.create(orgData, [:])

        then:
        AccessDeniedException ex = thrown()
        ex.message == 'Access Denied'

        when:
        orgSecureCrudApi.update(orgData, [:])

        then:
        ex = thrown()
        ex.message == 'Access Denied'

        when:
        orgSecureCrudApi.upsert(orgData, [:])

        then:
        ex = thrown()
        ex.message == 'Access Denied'

        when:
        orgSecureCrudApi.removeById(1L, [:])

        then:
        ex = thrown()
        ex.message == 'Access Denied'

        when:
        orgSecureCrudApi.bulkImport(DataOp.update, [orgData], [:], "Test")

        then:
        ex = thrown()
        ex.message == 'Access Denied'
    }

    Map getOrgData() {
        return [num:"T1", name:"T1", type: OrgType.Customer.name()]
    }

}
