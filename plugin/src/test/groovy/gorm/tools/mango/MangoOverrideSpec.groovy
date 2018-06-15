/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.mango

import gorm.tools.mango.api.MangoQuery
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.testing.unit.GormToolsTest
import grails.artefact.Artefact
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import grails.persistence.Entity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import spock.lang.Specification

class MangoOverrideSpec extends Specification implements GormToolsTest {

    void setupSpec() {
        defineBeans{ newMangoQuery(NewMangoQuery) }
        mockDomain(City)
    }

    void testMangoOverride() {
        setup:
        10.times {
            City city = new City(id: it, name: "Name#$it")
            city.save(failOnError: true)
        }

        when:
        List list = City.repo.query()
        then:
        list.size() == 1
        list[0].id == 2
    }

}

@Entity
class City {
    String name
}

class NewMangoQuery implements MangoQuery {

    @Override
    DetachedCriteria buildCriteria(Class domainClass, Map params, Closure closure = null) {
        new DetachedCriteria(domainClass).build { eq "id", 2 }
    }

    @Override
    List query(Class domainClass, Map params, Closure closure = null) {
        buildCriteria(domainClass, [:]).list()
    }
}

@Artefact("Repository")
@Transactional
class CityRepo extends DefaultGormRepo<City> {

    @Autowired
    NewMangoQuery newMangoQuery

    MangoQuery getMangoQuery(){ newMangoQuery }
}
