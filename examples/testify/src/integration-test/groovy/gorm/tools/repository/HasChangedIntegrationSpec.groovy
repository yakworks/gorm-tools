package gorm.tools.repository

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.testing.gorm.model.KitchenSink

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

    void "hasChanged change persist called twice or hasChanged called twice fails"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()
        //this will trigger auditstamp in repo and update the edited time.
        //sink.persist(flush:true) flush will fix it
        sink.persist()

        then:
        //this is fine
        !sink.hasChanged("num")
        //this is not - because repo has updated editedDate etc fields
        !sink.hasChanged()

        when:
        sink.num = "456"

        then:
        //why does this fail now?
        sink.hasChanged("num")
        sink.hasChanged()
    }
}
