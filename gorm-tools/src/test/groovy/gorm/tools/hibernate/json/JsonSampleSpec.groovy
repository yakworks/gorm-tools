/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.json

import gorm.tools.repository.GormRepo
import spock.lang.Specification
import yakworks.testing.gorm.RepoTestData
import yakworks.testing.gorm.unit.GormHibernateTest

class JsonSampleSpec extends Specification implements GormHibernateTest  {

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

    void "json to object"() {
        when:
        def o = new JsonSample(name: 'with addy')
        o.addy = new Addy(city: 'Denver', state: 'CO', zipCode: '80439')
        o.persist()
        def id = o.id
        flushAndClear()
        def o2 = JsonSample.get(id)

        then:
        o.id
        o2.id
        o.addy.city == 'Denver'
        o.addy.zipCode == '80439'
    }

}
