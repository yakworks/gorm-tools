package gorm.tools.repository

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.integration.DomainIntTest

import java.time.LocalDateTime

@Integration
@Rollback
class HasChangedIntegrationSpec extends Specification implements DomainIntTest {

    void "check if audit stamp marks it dirty"() {
        when:
        Org org = Org.create(num:"o-1", name:"o-1", companyId: 2, orgTypeId: OrgType.Customer.id)
        LocalDateTime editedDate = org.editedDate

        then:
        editedDate
        !org.hasChanged()

        when:
        org.validate()

        then:
        !org.hasChanged()

        when:
        org.persist()
        LocalDateTime editedDateUpdated = org.editedDate

        then:
        //editedDateUpdated != editedDate
        !org.hasChanged()
    }
}
