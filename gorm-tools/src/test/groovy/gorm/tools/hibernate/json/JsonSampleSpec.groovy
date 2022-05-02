/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.json

import gorm.tools.repository.GormRepo
import gorm.tools.testing.RepoTestData
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import testing.UuidSample

class JsonSampleSpec extends GormToolsHibernateSpec {

    //test the thing
    List<Class> getDomainClasses() { [JsonSample] }

    def "assert proper repos are setup"() {
        expect:
        JsonSample.repo instanceof GormRepo
        // UuidSample.repo instanceof IdGeneratorRepo
    }

    void "sanity check"() {
        when:
        def o = RepoTestData.build(JsonSample, save: false)
        o.json = [foo: 'bar']
        o.someList = [1, 2, 3]
        o.persist()
        def id = o.id
        flushAndClear()
        def o2 = JsonSample.get(id)

        then:
        o.id
        o2.id
        o2.json.foo == 'bar'
        o2.someList == [1, 2, 3]
    }

}
