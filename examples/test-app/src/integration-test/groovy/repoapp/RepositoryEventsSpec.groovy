package repoapp

import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class RepositoryEventsSpec extends Specification {

    void "check repo found"() {
        expect:
        Org.repo instanceof OrgRepo
        Org.repo instanceof GormRepo
    }

    void "check events"() {
        given:
        Map params = [name: "test"]

        when:
        Org org = Org.create(params)
        RepoUtil.flush()

        then: "Event listener should have been called"
        org.event == "PreDaoCreateEvent"
        org.id != null

        when:
        org = Org.update([id: org.id, name: "updated"])

        then:
        org.event == "PreDaoUpdateEvent"

        when:
        org.remove()

        then:
        org.event == "PostDaoRemoveEvent"
    }
}
