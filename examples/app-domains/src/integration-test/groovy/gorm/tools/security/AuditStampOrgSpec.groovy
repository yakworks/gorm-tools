package gorm.tools.security

import gorm.tools.security.testing.SecuritySpecHelper
import gorm.tools.testing.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification
import yakworks.taskify.domain.Org
import org.apache.commons.lang.time.DateUtils

@Integration
@Rollback
class AuditStampOrgSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    def "test create"(){
        when:
        Long id = Org.create([num:'123', name:"Wyatt Oil"]).id
        flushAndClear()

        then:
        def o = Org.get(id)
        o.num == '123'
        o.createdDate
        o.createdBy == 1
        o.editedDate
        o.editedBy == 1
        o.createdDate == o.editedDate
        //DateUtils.isSameInstant(o.createdDate, o.editedDate)

        when: 'its edited then edited should be updated'
        sleep(500)
        o.num = '999'
        o.persist(flush:true)

        then:
        o.refresh()
        o.num == '999'
        o.createdDate < o.editedDate
        //!DateUtils.isSameInstant(o.createdDate, o.editedDate)
        o.editedBy == 1
    }
}
