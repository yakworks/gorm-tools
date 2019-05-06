/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import gorm.tools.testing.unit.GormToolsTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger

class GparsBatchSupportSpec extends Specification implements GormToolsTest {

    @Autowired
    GparsBatchSupport asyncBatchSupport

    void setupSpec() {
        defineBeans({
            asyncBatchSupport(GparsBatchSupport)
        })
    }

    void "test collate"() {
        given:
        List list = createList(100)
        //asyncBatchSupport.transactionService = getDatastore().getService(TransactionService)

        expect:
        list.size() == 100

        when:
        list = asyncBatchSupport.collate(list, 10)

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
        asyncBatchSupport.parallel(asyncBatchSupport.collate(list, 10)) { List batch, Map args ->
            count.addAndGet(batch.size())
        }
        then:
        count.get() == 100
    }


    void "test parallel collate"() {
        given:
        List list = createList(100)

        expect:
        list.size() == 100

        when:
        AtomicInteger count = new AtomicInteger(0)
        asyncBatchSupport.parallelCollate([batchSize:10], list) { Map record, Map args ->
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
        asyncBatchSupport.batchTrx([test:1], list) { Map item, Map args ->
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
