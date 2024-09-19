/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.utils.BenchmarkHelper
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.KitchenSinkRepo
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

//XXX @SUD need help beefing this up and having tests for every method
class CrudApiSpec extends Specification implements GormHibernateTest {
    static entityClasses = [KitchenSink, SinkExt, SinkItem]
    static int SINK_COUNT = 100

    @Autowired KitchenSinkRepo kitchenSinkRepo

    // @Transactional
    void setupSpec() {
        BenchmarkHelper.startTime()
        KitchenSink.createKitchenSinks(SINK_COUNT)
        BenchmarkHelper.printEndTimeMsg("KitchenSeedData.createKitchenSinks")
    }

    void cleanupSpec() {
        KitchenSink.truncate()
    }

    DefaultCrudApi<KitchenSink> createCrudApi(){
        var crudApi = DefaultCrudApi.of(KitchenSink)
        return crudApi
    }

    void "sanity check"() {
        expect:
        SINK_COUNT == KitchenSink.count()
        SINK_COUNT == KitchenSink.list().size()
    }

    def "assert proper repos are setup"() {
        expect:
        KitchenSink.repo instanceof KitchenSinkRepo
        KitchenSink.repo.entityClass == KitchenSink
        KitchenSink.get(1)
    }

    def "test get"() {
        when:
        var crudApi = createCrudApi()
        CrudApi.CrudApiResult res = crudApi.get(1, [:])
        Map data = res.asMap()

        then:
        res.entity.id == data.id && data.id == 1
        data.num == '1'
        data.name == 'Blue Cheese'

        when: "includes are passed in"
        data = crudApi.get(1, [includes: 'num,name']).asMap()

        then: "should only be those 2 fields"
        data == [num: '1', name: 'Blue Cheese']
    }




}
