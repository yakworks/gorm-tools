package gorm.tools

import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org

@Integration
@Rollback
class RepositoryEventsSpec extends Specification {

    void "check repo found"() {
        expect:
        // Org.repo instanceof OrgRepo
        Org.repo instanceof GormRepo
    }

    void "check events"() {
        given:
        Map params = [num:'test1', name: "test", type: [id: 1]]

        when:
        Org org = Org.create(params)
        Org.repo.flush()

        then: "Event listener should have been called"
        org.comments == "BeforeBindEvent Create"
        // org.stampEvent == "BeforePersistEvent Stamp"
        org.id != null

        when:
        org = Org.repo.update([id: org.id, name: "updated"])

        then:
        org.comments == "BeforeBindEvent Update"

        when:
        org.remove()

        then:
        org.comments == "AfterRemoveEvent"
    }
}
