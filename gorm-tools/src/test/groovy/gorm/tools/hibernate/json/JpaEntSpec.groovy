/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.json

import gorm.tools.repository.GormRepo
import gorm.tools.testing.RepoTestData
import gorm.tools.testing.hibernate.GormToolsHibernateSpec

class JpaEntSpec extends GormToolsHibernateSpec {

    //test the thing
    List<Class> getDomainClasses() { [JpaEnt] }

    // def "assert proper repos are setup"() {
    //     expect:
    //     JpaEnt.repo instanceof GormRepo
    //     // UuidSample.repo instanceof IdGeneratorRepo
    // }

    void "sanity check"() {
        when:
        def o = RepoTestData.build(JpaEnt, save: false)
        o.json = [foo: 'bar']
        o.save(failOnError: true)
        def id = o.id
        flushAndClear()
        def o2 = JpaEnt.get(id)


        then:
        o.id
        o2.id
        o2.json.foo == 'bar'
    }

}