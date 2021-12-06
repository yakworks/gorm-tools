package gorm.tools.csv


class CSVMapReaderBatchTest extends GroovyTestCase {

    void testBatchChunks() {
        def csv = new CSVMapReader(new StringReader(testCsv), [batchSize: 3])

        csv.eachWithIndex { list, i ->
            if (i == 0) {
                assertEquals(3, list.size())
                assertEquals([col1: '3', col2: 'chunk1', col3: 'val2'], list[2])
            }
            if (i == 2) {
                assertEquals(2, list.size())
                assertEquals([col1: '7', col2: 'chunk3', col3: 'val2'], list[0])
            }
        }

    }

    void testToList() {
        def list = new CSVMapReader(new StringReader(testCsv), [batchSize: 3]).toList()

        assertEquals(3, list.size())

        assertEquals(3, list[0].size())
        assertEquals(3, list[1].size())
        assertEquals(2, list[2].size())
    }

    void testToListBigger() {
        def list = new CSVMapReader(new StringReader(testCsv), [batchSize: 20]).toList()

        assertEquals(1, list.size())
        assertEquals(8, list[0].size())
    }


    String testCsv = '''\
col1,col2,col3
1,"chunk1","val1"
2,"chunk1",val1
3,"chunk1",val2
4,"chunk2",val2
5,"chunk2",val2
6,"chunk2",val2
7,"chunk3",val2
8,"chunk3",val2

'''

}
