package gorm.tools.audit

import spock.lang.Ignore
import yakworks.testing.gorm.integration.SecuritySpecHelper
import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

import java.time.LocalDate

@Integration
@Rollback
@Ignore
class AuditStampOrgSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    void "test create"() {
        when:
        println "AuditStampOrgSpec"
        Long id = Org.create([num: '123', name: "Wyatt Oil", type: OrgType.Customer]).id
        flushAndClear()

        then:
        def o = Org.get(id)
        o.num == '123'
        o.createdDate
        o.createdBy == 1
        o.editedDate
        o.editedBy == 1
        o.createdDate == o.editedDate
        o.createdByUser.username == 'admin'
        o.editedByUser.username == 'admin'
        //DateUtils.isSameInstant(o.createdDate, o.editedDate)

        when: 'its edited then edited should be updated'
        sleep(500)
        o.num = '999'
        o.persist(flush: true)

        then:
        o.refresh()
        o.num == '999'
        o.createdDate < o.editedDate
        //!DateUtils.isSameInstant(o.createdDate, o.editedDate)
        o.editedBy == 1
    }

    void "auditstamp fields should not be bindable"() {
        given:
        LocalDate now = LocalDate.now()

        when:
        Org org = Org.create([num        : '123', name: "Wyatt Oil", type: OrgType.Customer,
                              createdDate: "2020-01-01",
                              editedDate : "2020-01-01",
                              createdBy  : 1000,
                              editedBy   : 1000,
                              flex       : [text1      : "flex1",
                                            createdDate: "2020-01-01",
                                            editedDate : "2020-01-01",
                                            editedBy   : 1000,
                                            createdBy  : 1000,
                              ],
        ])

        then:
        noExceptionThrown()
        org
        org.createdDate.toLocalDate() == now
        org.editedDate.toLocalDate() == now
        org.createdBy != 1000

        and:
        org.flex
        org.flex.text1 == "flex1"
        org.flex.createdDate.toLocalDate() == now
        org.flex.editedDate.toLocalDate() == now
        org.createdBy != 1000

        and:
        !org.hasErrors()
        !org.flex.hasErrors()
    }
}
