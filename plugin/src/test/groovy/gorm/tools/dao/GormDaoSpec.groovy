package gorm.tools.dao

import gorm.tools.databinding.FastBinder
import grails.test.hibernate.HibernateSpec
import grails.testing.spring.AutowiredTest
import spock.lang.Specification
import testing.Location
import testing.Nested
import testing.Org
import testing.OrgDao

class GormDaoSpec extends HibernateSpec implements AutowiredTest {

    def doWithSpring = {
        orgDao(OrgDao) { bean ->
            bean.autowire = true
        }
        locationDao(DefaultGormDao, Location) { bean ->
            bean.autowire = true
        }
        nestedDao(DefaultGormDao, Nested) { bean ->
            bean.autowire = true
        }
        daoEventInvoker(DaoEventInvoker){ bean ->
            bean.autowire = true
        }
        daoUtilBean(DaoUtil)
        fastBinder(FastBinder)
    }

    List<Class> getDomainClasses() { [Org,Location,Nested] }

    void setup() {
        //Org.dao.insertTestData()
    }


    def "test create"() {
        when:
        Map p = [name:'foo']
        p.location = new Location(city: "City", nested: new Nested(name: "Nested", value: 1)).save()
        Org.dao.create(p)

        then:
        Org.findByName("foo")
    }

}
