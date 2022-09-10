/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import java.util.concurrent.atomic.AtomicInteger

import org.springframework.context.ApplicationContext

import yakworks.gorm.testing.hibernate.GormToolsHibernateSpec
import grails.testing.spring.AutowiredTest
import spock.lang.Ignore
import testing.CustType

@Ignore // not used right now.
class GparsParallelToolsSpec extends GormToolsHibernateSpec implements AutowiredTest {

    // GparsParallelTools parallelTools

    List<Class> getDomainClasses() { [CustType] }

    void setup() {
        //parallelTools = ctx.getBean("parallelTools")
        parallelTools.asyncEnabled = true
    }

    // void cleanup() {
    //     parallelTools.asyncEnabled = false
    // }

    void "test collate"() {
        given:
        List list = createList(100)
        //parallelTools.transactionService = getDatastore().getService(TransactionService)

        expect:
        list.size() == 100

        when:
        list = parallelTools.slice(list, 10)

        then:
        list.size() == 10

    }

    void "test parallel"() {
        given:
        List list = createList(100)

        expect:
        list.size() == 100

        when:
        AtomicInteger count = new AtomicInteger(0)
        def slicedList = parallelTools.slice(list, 10)
        assert slicedList.size() == 10

        parallelTools.each(slicedList) { List batch ->
            count.addAndGet(batch.size())
        }

        then:
        count.get() == 100
    }

    void "test eachParallel"() {
        given:
        List<Map> list = createList(100)

        expect:
        list.size() == 100

        when:
        AtomicInteger count = new AtomicInteger(0)
        parallelTools.each(list) { Map item ->
            new CustType(name: "name $item.name").persist()
        }
        then:
        CustType.count() == 100
    }


    // void "test parallel collate"() {
    //     given:
    //     List list = createList(100)
    //
    //     expect:
    //     list.size() == 100
    //
    //     when:
    //     AtomicInteger count = new AtomicInteger(0)
    //     def args = AsyncConfig.of(CustType.repo.datastore).sliceSize(10).enabled(false)
    //
    //     parallelTools.slicedEach(args, list) { Map record ->
    //         count.addAndGet(1)
    //     }
    //     then:
    //     count.get() == 100
    // }

    void "test sliceBatchClosure"() {
        given:
        List list = createList(10)
        ApplicationContext mockContext = Mock()

        when:
        int count = 0
        def sliceClosure = parallelTools.sliceClosure{ Map item ->
            count = count + 1
        }

        sliceClosure.call( list )


        then:
        count == 10

    }


    List<Map> createList(int num) {
        List result = []

        for(int i in (1..num)) {
            result << [name:"Record-$i"]
        }

        return result
    }
}
