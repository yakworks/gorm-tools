/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import gorm.tools.repository.model.IdGeneratorRepo
import spock.lang.Specification
import yakworks.testing.gorm.model.Thing
import yakworks.testing.gorm.model.ThingRepo
import yakworks.testing.gorm.unit.GormHibernateTest

class IdGenRepoSpec extends Specification implements GormHibernateTest {

    //test the thing
    static List entityClasses = [Thing]

    def "assert proper repos are setup"() {
        expect:
        Thing.repo instanceof ThingRepo
        Thing.repo instanceof IdGeneratorRepo
    }

    def "test getIdGeneratorKey()"() {
        expect:
        'Thing.id' == Thing.repo.getIdGeneratorKey()
    }

    def "test generateId()"() {
        expect:
        1000 == Thing.repo.generateId()
    }

    def "test generateId(Ent)"() {
        when:
        def ent = new Thing()

        then:
        1001 == Thing.repo.generateId(ent)
        //should be same if called again as it checks if id is null
        1001 == Thing.repo.generateId(ent)
        ent.id == 1001
    }
}
