/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.idgen

import gorm.tools.testing.support.MockJdbcIdGenerator
import gorm.tools.testing.unit.DataRepoTest
import groovyx.gpars.GParsPool
import spock.lang.Shared
import spock.lang.Specification
import testing.Cust

class PooledIdGeneratorSpec extends Specification implements DataRepoTest {

    // @Shared
    // MockJdbcIdGenerator jdbcIdGenerator
    // @Shared
    // PooledIdGenerator idGenerator

    @Shared
    MockJdbcIdGenerator mockdbgen

    @Shared
    PooledIdGenerator batchgen

    // List<Class> getDomainClasses() { [Org] }

    void setupSpec() {
        mockDomains Cust
        mockdbgen = new MockJdbcIdGenerator()
        mockdbgen.table.put("table.id", 1)
        mockdbgen.table.put("table1.id", 99)
        batchgen = new PooledIdGenerator(mockdbgen)

        //
        // batchgen = idGenerator
        //mockdbgen.transactionManager = getTransactionManager()
        //batchgen.setBatchSize(5)

    }

    // def setup() {
    //     assert jdbcIdGenerator
    //     jdbcIdGenerator.table.put("table.id", 1)
    //     jdbcIdGenerator.table.put("table1.id", 99)
    //     assert idGenerator
    //     batchgen = idGenerator
    // }

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
        GParsPool.withPool(10) {
            (0..99).eachParallel {
                ids.add(batchgen.getNextId("table2.id"))
            }
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
