package gorm.tools.mango

import gorm.tools.dao.GormDao
import gorm.tools.testing.DaoDataTest
import grails.artefact.Artefact
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import grails.persistence.Entity
import spock.lang.Specification

class MangoOverrideSpec extends Specification implements DaoDataTest {

    void setup() {
        mockDomain(City)
    }

    void testMangoOverride() {
        setup:
        10.times {
            City City = new City(id: it, name: "Name#$it")
            City.save(failOnError: true)
        }

        when:
        List list = City.dao.query()
        then:
        list.size() == 1
        list[0].id == 2
    }

}

@Entity
class City {
    String name
}

class NewMangoQuery implements MangoQueryApi {

    @Override
    DetachedCriteria buildCriteria(Class domainClass, Map params, Closure closure = null) {
        new DetachedCriteria(domainClass).build { eq "id", 2 }
    }

    @Override
    List query(Class domainClass, Map params, Closure closure = null) {
        buildCriteria(domainClass, [:]).list()
    }
}

@Artefact("Dao")
@Transactional
class CityDao implements GormDao<City> {
    MangoQueryApi getMangoQuery(){ new NewMangoQuery()}
}
