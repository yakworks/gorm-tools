package daoapp

import gorm.tools.dao.DaoUtil
import gorm.tools.dao.GormDao
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class DaoEventsSpec extends Specification {

    void "check dao found"() {
        expect:
        Org.dao instanceof OrgDao
        Org.dao instanceof GormDao
    }

    void "check events"() {
        given:
        Map params = [name: "test"]

        when:
        Org org = Org.create(params)
        DaoUtil.flush()

        then: "Event listener should have been called"
        org.event == "PreDaoCreateEvent"
        org.id != null

        when:
        org = Org.update([id:org.id, name:"updated"])

        then:
        org.event == "PreDaoUpdateEvent"

        when:
        org.remove()

        then:
        org.event == "PostDaoRemoveEvent"
    }
}
