package gorm.tools.csv


class CSVMapReaderTest extends GroovyTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testConvertArrayToMap() {
        //CSVMapReader csvreader = new CSVMapReader()
        String[] keys = ['col1', 'col2'].toArray()
        String[] vals = ['val1', 'val2'].toArray()

        def map = CSVMapReader.convertArrayToMap(keys, vals)

        assertEquals(2, map.size())
        assertEquals([col1: 'val1', col2: 'val2'], map)
    }

    void testEach() {
        CSVMapReader cmr = new CSVMapReader(new StringReader(testCsv1row))
        cmr.each { map ->
            assertEquals([col1: 'val1', col2: 'val2', col3: 'val3'], map)
        }
    }

    void testReadAll() {
        CSVMapReader cmr = new CSVMapReader(new StringReader(testCsvMulti))
        def list = cmr.readAll()
        csvMultiAsserts(list)
    }

    void testToList() {
        CSVMapReader cmr = new CSVMapReader(new StringReader(testCsvMulti))
        def list = new CSVMapReader(new StringReader(testCsvMulti)).toList()
        csvMultiAsserts(list)
    }

    void testAsType() {
        def list = new CSVMapReader(new StringReader(testCsvMulti)) as List
        csvMultiAsserts(list)
    }

    void testCustomFields() {
        def csvMapReader = new CSVMapReader(new StringReader(csvDelimPipe), [separatorChar: '|'])
        csvMapReader.fieldKeys = ['gogo', 'gadget', 'csv']
        def list2 = csvMapReader.toList() //<- alternate for a list of maps
        assert [gogo: 'val1', gadget: 'val2', csv: 'val3'] == list2[0]
    }

    void csvMultiAsserts(list) {
        assert list.size() == 2
        list.eachWithIndex { map, i ->
            if (0 == i) {
                assertEquals([col1: 'val1', col2: 'val2', col3: 'val3'], map)
            } else if (1 == i) {
                assertEquals([col1: '10', col2: '20', col3: '30,3'], map)
            }
        }
    }

    //test iterator
    def testForLoopIterator() {
        def csv = new CSVMapReader(new StringReader(testCsvMulti))
        int i = 0
        for (map in csv) {
            if (0 == i) {
                assertEquals([col1: 'val1', col2: 'val2', col3: 'val3'], map)
            } else if (1 == i) {
                assertEquals([col1: '10', col2: '20', col3: '30,3'], map)
            }
            i++
        }
    }

    def testCsv1row = '''\
col1,col2,col3
"val1",val2,"val3"
'''

    def testCsvMulti = '''\
col1,col2,col3
"val1",val2,"val3"
"10",20,"30,3"

'''

    def csvDelimPipe = '''\
"val1"|val2|"val3"
"10",20,"30,3"
'''

}
