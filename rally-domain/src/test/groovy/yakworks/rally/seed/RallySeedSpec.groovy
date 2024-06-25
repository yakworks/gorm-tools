package yakworks.rally.seed

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

//@Ignore //see FIXME line 99 in GormHibernateTest
class RallySeedSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = RallySeed.entityClasses
    static List springBeans = RallySeed.springBeanList

    @Autowired JdbcTemplate jdbcTemplate

    void setupSpec() {
        RallySeed.fullMonty(10)
    }

    void setup(){
        assert jdbcTemplate
        // RallySeedData.fullMonty(10)
        // flushAndClear()
    }

    void "smoke test"() {
        when:
        def o = Org.get(1)
        Integer cnt = Org.count()

        then:
        o
        o.name
        Org.count() == 10
        Org.list().each { Org org ->
            assert org.contact.id
        }
    }

    void "Contacts"() {
        expect:
        Contact.count() == 20
    }

}
