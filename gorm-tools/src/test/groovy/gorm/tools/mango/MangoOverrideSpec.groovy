/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import gorm.tools.beans.Pager

import javax.inject.Inject

import gorm.tools.mango.api.MangoQuery
import gorm.tools.mango.api.QueryArgs
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.model.RepoEntity
import yakworks.gorm.testing.unit.DataRepoTest
import grails.persistence.Entity
import spock.lang.Specification

class MangoOverrideSpec extends Specification implements DataRepoTest {

    void setupSpec() {
        defineBeans{ newMangoQuery(NewMangoQuery) }
        mockDomain(City)
    }

    void testMangoOverride() {
        setup:
        10.times {
            City city = new City(id: it, name: "Name$it")
            city.save(failOnError: true)
        }

        when:
        List list = City.repo.queryList()
        then:
        list.size() == 1
        list[0].id == 2
    }

}

@Entity
class City implements RepoEntity<City> {
    String name
}

class NewMangoQuery implements MangoQuery {

    @Override
    MangoDetachedCriteria query(Class domainClass, Map params, Closure closure = null) {
        new MangoDetachedCriteria(domainClass).build { eq "id", 2 }
    }

    @Override
    MangoDetachedCriteria query(Class domainClass, QueryArgs qargs, Closure closure = null) {
        new MangoDetachedCriteria(domainClass).build { eq "id", 2 }
    }

    @Override
    List list(MangoDetachedCriteria criteria, Pager pager) {
        criteria.list(max: pager.max, offset: pager.offset)
    }

    // @Override
    // List queryList(Class domainClass, Map params, Closure closure = null) {
    //     query(domainClass, [:], null).list()
    // }

    // @Override
    // List queryList(Class domainClass, QueryArgs qargs, Closure closure = null) {
    //     query(domainClass, [:], null).list()
    // }
}

@GormRepository
class CityRepo implements GormRepo<City> {

    @Inject
    NewMangoQuery newMangoQuery

    MangoQuery getMangoQuery(){ newMangoQuery }
}
