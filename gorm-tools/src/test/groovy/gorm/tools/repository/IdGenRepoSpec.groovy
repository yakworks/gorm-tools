/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import gorm.tools.repository.model.IdGeneratorRepo
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import testing.IdGenTest

//import static grails.buildtestdata.TestData.build

import testing.IdGenTestRepo

class IdGenRepoSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [IdGenTest] }

    def "assert proper repos are setup"() {
        expect:
        IdGenTest.repo instanceof IdGenTestRepo
        IdGenTest.repo instanceof IdGeneratorRepo
    }

    def "test getIdGeneratorKey()"() {
        expect:
        'IdGenTest.id' == IdGenTest.repo.getIdGeneratorKey()
    }

    def "test generateId()"() {
        expect:
        1 == IdGenTest.repo.generateId()
    }

    def "test generateId(Ent)"() {
        when:
        def ent = new IdGenTest()

        then:
        2 == IdGenTest.repo.generateId(ent)
        //should be same if called again as it checks if id is null
        2 == IdGenTest.repo.generateId(ent)
        ent.id == 2
    }
}
