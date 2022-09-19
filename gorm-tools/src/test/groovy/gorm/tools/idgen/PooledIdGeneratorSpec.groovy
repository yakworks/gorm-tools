/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.idgen

import gorm.tools.async.ParallelStreamTools
import gorm.tools.config.AsyncConfig
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Specification
import testing.Cust
import yakworks.testing.gorm.support.MockJdbcIdGenerator

// import groovyx.gpars.GParsPool

import yakworks.testing.gorm.unit.GormHibernateTest

class PooledIdGeneratorSpec extends Specification implements GormHibernateTest {
    static List entityClasses = [Cust]

    @Autowired ParallelStreamTools parallelTools
    @Autowired AsyncConfig asyncConfig

    @Shared
    MockJdbcIdGenerator mockdbgen

    @Shared
    PooledIdGenerator batchgen

    void setupSpec() {
        mockdbgen = new MockJdbcIdGenerator()
        mockdbgen.table.put("table.id", 1)
        mockdbgen.table.put("table1.id", 99)
        batchgen = new PooledIdGenerator(mockdbgen)
        //
        // batchgen = idGenerator
        //mockdbgen.transactionManager = getTransactionManager()
        //batchgen.setBatchSize(5)

    }

    void "test getNextId"() {
        setup:
        batchgen.setBatchSize("table.id", 2)
        batchgen.setBatchSize("table1.id", 4)

        expect:
        id == batchgen.getNextId(keyName)

        where:
        id   |  keyName
        // with batchSize=2
        1    |  "table.id"
        2    |  "table.id"
        // new batch
        3    |  "table.id"
        4    |  "table.id"
        // new batch
        5    |  "table.id"

        // with batchSize=4
        99   |  "table1.id"
        100  |  "table1.id"
        101  |  "table1.id"
        102  |  "table1.id"
        // new batch
        103  |  "table1.id"
    }

    void "test getNextId with multithreading"() {
        setup:
        mockdbgen.table.put("table2.id", 1)
        batchgen.setBatchSize("table2.id", 17)
        List ids = Collections.synchronizedList([])

        when:
        asyncConfig.enabled = true
        parallelTools.each(0..99) {
            ids.add(batchgen.getNextId("table2.id"))
        }

        mockdbgen.getNextId('table2.id', 17)
        then:
        ids.size() == 100

        (1L..100L).each { id ->
            assert ids.contains(id)
        }

        // getNextId should return 101, because previously we called getNextId 100 times using several threads
        batchgen.getNextId("table2.id") == 101
    }

}
