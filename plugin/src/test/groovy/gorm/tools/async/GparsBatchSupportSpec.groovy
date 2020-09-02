/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import java.util.concurrent.atomic.AtomicInteger

import org.springframework.context.ApplicationContext

import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.artefact.Artefact
import grails.testing.spring.AutowiredTest

class GparsAsyncSupportSpec extends GormToolsHibernateSpec implements AutowiredTest {

    GparsAsyncSupport asyncSupport

    List<Class> getDomainClasses() { [Foo] }

    void setup() {

        //asyncSupport = ctx.getBean("asyncSupport")
    }

    void "test collate"() {
        given:
        List list = createList(100)
        //asyncSupport.transactionService = getDatastore().getService(TransactionService)

        expect:
        list.size() == 100

        when:
        list = asyncSupport.collate(list, 10)

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
        asyncSupport.parallel(asyncSupport.collate(list, 10)) { List batch, Map args ->
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
        asyncSupport.eachParallel(list) { Map item ->
            new Foo(name: "name $item.name").persist()
        }
        then:
        Foo.count() == 100
    }


    void "test parallel collate"() {
        given:
        List list = createList(100)

        expect:
        list.size() == 100

        when:
        AtomicInteger count = new AtomicInteger(0)
        asyncSupport.parallelCollate([batchSize:10], list) { Map record, Map args ->
            count.addAndGet(1)
        }
        then:
        count.get() == 100
    }


    void "test withTrx"() {
        given:
        List list = createList(10)
        ApplicationContext mockContext = Mock()

        when:
        int count = 0
        asyncSupport.batchTrx([test:1], list) { Map item, Map args ->
            count = count + 1
            assert args.test == 1
        }

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

@Artefact("Domain")
class Foo {
    String name
}
