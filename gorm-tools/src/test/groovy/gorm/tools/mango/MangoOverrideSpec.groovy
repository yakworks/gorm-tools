/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import gorm.tools.beans.Pager
import gorm.tools.mango.api.QueryService
import gorm.tools.mango.api.QueryArgs
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.model.RepoEntity
import yakworks.testing.gorm.unit.DataRepoTest
import grails.persistence.Entity
import spock.lang.Specification

class MangoOverrideSpec extends Specification implements DataRepoTest {

    void setupSpec() {
        defineBeans{ newMangoQuery(NewMangoQuery) }
        mockDomain(MangoThing)
    }

    void testMangoOverride() {
        setup:
        10.times {
            MangoThing city = new MangoThing(id: it, name: "Name$it")
            city.save(failOnError: true)
        }

        when:
        List list = MangoThing.repo.query([:]).list()
        then:
        list.size() == 1
        list[0].id == 2
    }

}

@Entity
class MangoThing implements RepoEntity<MangoThing> {
    String name
}

class NewMangoQuery extends DefaultQueryService<MangoThing> {

    NewMangoQuery() {
        super(MangoThing)
    }

    @Override
    MangoDetachedCriteria query(QueryArgs qargs, Closure closure = null) {
        new MangoDetachedCriteria(MangoThing).build { eq "id", 2 }
    }

    // @Override
    // List pagedList(MangoDetachedCriteria criteria, Pager pager) {
    //     criteria.list(max: pager.max, offset: pager.offset)
    // }

}

@GormRepository
class MangoThingRepo implements GormRepo<MangoThing> {

    // @Autowired
    // QueryService queryService

    //MangoQuery getMangoQuery(){ newMangoQuery }
}
