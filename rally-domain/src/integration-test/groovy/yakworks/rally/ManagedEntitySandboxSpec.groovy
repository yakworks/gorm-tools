package yakworks.rally

import java.time.LocalDate

import gorm.tools.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.Hibernate
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@Integration
@Rollback
@Ignore
class ManagedEntitySandboxSpec extends Specification implements DataIntegrationTest {

    Org createOrg(){
        def id=2
        def data = [
            id: id,
            num: "$id",
            name: "name-$id",
            type: OrgType.Customer,
            comments: (id % 2) ? "Lorem ipsum dolor sit amet $id" : null ,
            inactive: (id % 2 == 0),
            info: [phone: "1-800-$id"],
            // flex: [
            //     num1: (id - 1) * 1.25,
            //     num2: (id - 1) * 1.5,
            //     date1: LocalDate.now().plusDays(id).toString()
            // ],
            // location: [city: "City$id"]
        ]
        return Org.create(data, bindId: true)
    }
    void "getting should return not proxy"() {
        when:
        createOrg()
        // def o = new Org(id:2, num: '123', name: 'foo', type: OrgType.Customer)
        // o.persist(flush: true)
        flushAndClear()
        def proxy = Org.load(2)

        then: "load returns a proxy"
        !Hibernate.isInitialized(proxy)

        when:
        def info = proxy.info

        then: "getId should also not unwrap the proxy"
        info
        !GrailsHibernateUtil.isInitialized(proxy, "info")
        // Hibernate.isInitialized(infoproxy)

        // when:
        // proxy.name
        //
        // then: "name access will trigger load"
        // Hibernate.isInitialized(proxy)
    }

}
