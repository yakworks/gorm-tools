package gorm.tools.idgen

import gorm.tools.testing.unit.idgen.MockIdGenerator
import grails.test.hibernate.HibernateSpec
import spock.lang.Shared

class PooledIdGeneratorSpec extends HibernateSpec {

    @Shared
    MockIdGenerator mockdbgen
    @Shared
    PooledIdGenerator batchgen

    void setupSpec() {
        mockdbgen = new MockIdGenerator()
        batchgen = new PooledIdGenerator(mockdbgen)
        mockdbgen.transactionManager = getTransactionManager()
        //batchgen.setBatchSize(5)

    }

    def testGetNextIdStringInt() {
        //the id waiting will be 1 and this will increment to 3
        assertTrue(1 == batchgen.getNextId("table.id", 2))
        //so this following should get 3 back and set the next avail id to 3+2=5
        assertEquals(new Long(3), new Long(batchgen.getNextId("table.id", 3)))
        //batch size is 5 so the db will hold 6 and nothing has changed yet
        assertEquals(new Long(6), mockdbgen.table.get("table.id"))
        //the next get should trigger a db increment and the mockdb table should now hold 6+5 =11
        assertEquals(new Long(6), new Long(batchgen.getNextId("table.id")))
        assertEquals(new Long(11), mockdbgen.table.get("table.id"))

        assertEquals(new Long(11), new Long(batchgen.getNextId("table.id", 100)))
        assertEquals(new Long(111), mockdbgen.table.get("table.id"))
        //another user calls and should get 111
        assertEquals(new Long(111), new Long(batchgen.getNextId("table.id")))
        //which would trigger  increment of 5
        assertEquals(new Long(116), mockdbgen.table.get("table.id"))

    }

    def testGetIncrementPastBatchSize() {
        //positon the next Id to 5
        assertTrue(1 == batchgen.getNextId("table.id", 3))
        assertEquals(new Long(6), mockdbgen.table.get("table.id")) //should be 6
        //simulate a db process going in and updating the record
        mockdbgen.table.put("table.id", 50)
        //we tell it to increment 100 so since we cant be sure that the table hasn't been updates
        //it need to discard the 4 and 5 and get whatever is in the table from mockjdbgen which is 6
        assertTrue(50 == batchgen.getNextId("table.id", 100)) //so now we can use 6 to 105
        assertEquals(new Long(150), mockdbgen.table.get("table.id"))
    }

    def testGetIncrementInsideBatchSize() {
        //positon the next Id to 4
        assertTrue(1 == batchgen.getNextId("table.id", 3))
        assertEquals(new Long(6), mockdbgen.table.get("table.id")) //should be 6

        //positon the next Id to 4 so the next call should tell us that we have 4 and 5 to use.
        assertEquals(new Long(4), new Long(batchgen.getNextId("table.id", 2)))
        assertEquals(new Long(6), mockdbgen.table.get("table.id")) //still be 6

        //and boom the next call should trigger a new batch grab and set db to 12
        assertEquals(new Long(6), new Long(batchgen.getNextId("table.id")))
        assertEquals(new Long(11), mockdbgen.table.get("table.id"))
    }

    def testGetNextIdString() {

        for (int i = 1; i < 10; i++) {
            assertEquals(new Long(i), new Long(batchgen.getNextId("table.id")))
            if (i < 6) {
                assertEquals(new Long(6), mockdbgen.table.get("table.id"))
            } else {
                assertEquals(new Long(11), mockdbgen.table.get("table.id"))
            }

        }
        assertTrue(1000 == batchgen.getNextId("xxx.id"))//default seed size
        assertEquals(new Long(1005), mockdbgen.table.get("xxx.id"))

        assertTrue(1001 == batchgen.getNextId("xxx.id"))//default seed size
        assertEquals(new Long(1005), mockdbgen.table.get("xxx.id"))
    }

}
