/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango


import gorm.tools.mango.api.QueryArgs
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.api.problem.data.DataProblemException
import yakworks.json.groovy.JsonEngine

class QueryArgsSpec extends Specification {

    void "parseParams q is map"() {
        when:
        QueryArgs qargs = QueryArgs.of(
            q: [id: 24],
            max: 10,
            sort:'foo',
            page: 2,
            offset: 10
        )
        then:
        //parsed.criteria.id == 24
        qargs.sort == ['foo':'asc']
        qargs.buildCriteriaMap() == [id: 24]
        qargs.pager

        when: 'q is used and reserved pager is passed'
        qargs = QueryArgs.of(q: [id: 24, offset: 'testing'], max: 10, sort:'foo', page: 2, offset: 10)

        then: 'it should have them in criteria'
        //parsed.criteria.id == 24
        qargs.sort == ['foo':'asc']
        qargs.buildCriteriaMap() == [id: 24, offset: 'testing']

        qargs.pager.max == 10
        qargs.pager.offset == 10
        qargs.pager.page == 2
    }

    void "parseParams q is json"() {

        when: 'q is JSON'
        QueryArgs qargs = QueryArgs.of(q: "{id: 24, something: 'testing'}", sort:'foo:desc' )

        then: 'it should have them in criteria'
        //sort not in the qCriteria
        qargs.criteriaMap == [id:24, something: 'testing']
        //sort is in the buildCriteria()
        qargs.sort == ['foo':'desc']
        qargs.buildCriteriaMap() == [id: 24, something: 'testing']

        when: 'not using q and strict=false default'
        //when not using q or criteria
        qargs = QueryArgs.of(name: 'joe', sort:'foo', page: 2)

        then: "adds the params to the criteria"
        qargs.criteriaMap == [name: 'joe']
        qargs.sort == ['foo':'asc']
        qargs.buildCriteriaMap() == [name: 'joe']

        qargs.pager.max == 20
        qargs.pager.offset == 20 //(max * (page - 1))
        qargs.pager.page == 2


        when: "strict is false by default"
        qargs = QueryArgs.of(name: 'joe')
        then: "so q param is not required and props will be added"
        qargs.buildCriteriaMap() == [name: 'joe']
        qargs.pager
    }

    void "q with invalid json"() {
        when:
        QueryArgs.of(q:'''({'state': [0], 'createdDate': {'$between': ['2024-10-02', '2024-10-03']})''')

        // then:
        // IllegalArgumentException ex = thrown()
        // ex.message.contains 'Json parsing expected'
        then:
        def e = thrown(DataProblemException)
        e.code == 'error.query.invalid'
        e.detail.startsWith("Invalid JSON. Error parsing query")
    }

    void "q with crazy ERNDC query"() {
        when:
        QueryArgs qargs = QueryArgs.of(q:'''{"$or":[{"docType":{"$ne":"PA"},"tranDate":{"$gte":"2024-07-11","$lte":"2024-10-09",},"state":0,"status.id":{"$ne":29},"customer.num":"10099718"}]''')

        then:
        qargs.criteriaMap == [
            $or:[
                [
                    tranDate:[$lte:'2024-10-09', $gte:'2024-07-11'],
                    state:0,
                    'status.id':[$ne:29],
                    docType:[$ne:'PA'],
                    'customer.num':'10099718'
                ]
            ]
        ]
        JsonEngine.toJson(qargs.criteriaMap) == '''{"$or":[{"tranDate":{"$lte":"2024-10-09","$gte":"2024-07-11"},"state":0,"status.id":{"$ne":29},"docType":{"$ne":"PA"},"customer.num":"10099718"}]}'''

    }

    void "parseParams when no q and strict=false, just a map"() {

        when: 'not using q'
        //when not using q then it pulls out the sort, order and pager info and leaves the rest as is
        QueryArgs qargs = QueryArgs.of(id: 123, name: 'joe', max: 10, sort:'bar:desc,foo:asc')

        then:
        qargs.sort == ['foo':'asc', bar:'desc']
        qargs.buildCriteriaMap() == [id: 123, name: 'joe']
        qargs.pager.max == 10

        when:
        qargs = QueryArgs.of(name: 'joe')
        then:
        qargs.buildCriteriaMap() == [name: 'joe']
    }

    @Ignore //this will no longer work. Requires qSearch to be used now so we can validate q.
    void "parseParams q is string so its quick search"() {
        when: 'no qSearchFields are passed in params'
        QueryArgs qargs = QueryArgs.of(q:"foo")

        then:
        qargs.buildCriteriaMap() == ['$qSearch': 'foo']
        qargs.pager
    }

    def "parseParams when no q and strict=true"() {

        when:
        QueryArgs qargs = new QueryArgs().strict(true).build(name: 'joe')
        then:
        qargs.criteriaMap == [:]
        qargs.buildCriteriaMap() == [:]

        when: 'not using q'
        //when not using q then it pulls out the sort, order and pager info and leaves the rest as is
        qargs = new QueryArgs().strict(true).build(id: 123, name: 'joe', max: 10, sort:'bar')

        then: "the params wont get added to criteria"
        qargs.criteriaMap == [:]
        qargs.sort == [bar:'asc']
        qargs.buildCriteriaMap()  == [:]
        qargs.pager.max == 10

    }


    def "build with both q and qsearch"() {

        when: 'no qSearchFields'
        def qjson = "{id: 1, name: 'joe'}"
        def qargs = new QueryArgs()
        // qargs.qSearchFields = ['name','num']
        qargs.build(q: qjson, qSearch:'foo')

        then:
        qargs.buildCriteriaMap() == [id: 1, name: 'joe', '$qSearch': 'foo']
        qargs.pager

    }

    def "test buildSort simple"() {

        when: 'simple'
        def qargs = new QueryArgs()

        then:
        qargs.buildSort('name') == [name: 'asc']
        qargs.buildSort('name', 'desc') == [name: 'desc']
    }

    def "test buildSort"() {

        when: 'simple'
        def qargs = new QueryArgs()

        then:
        qargs.buildSort('name: asc') == [name: 'asc']
        qargs.buildSort('name:asc') == [name: 'asc']
        qargs.buildSort('name:asc, num: desc') == [name: 'asc', num: 'desc']

        qargs.buildSort('{name:"asc", num: "desc"}') == [name: 'asc', num: 'desc']
        qargs.buildSort('name, num') == [name: 'asc', num: 'asc']
        qargs.buildSort('inactive desc, id desc') == [inactive: 'desc', id: 'desc']
    }

    def "test buildProjections"() {

        when: 'simple'
        def qargs = new QueryArgs()

        then:
        qargs.buildProjections('name: "group"') == [name: 'group']
        qargs.buildProjections('name:"group", amount: "sum"') == [name: "group", amount: "sum"]
        qargs.buildProjections('{name:"group", amount: "sum"}') == [name: 'group', amount: 'sum']
    }

    def "test buildSelect"() {

        when: 'simple'
        def qargs = new QueryArgs()

        then:
        qargs.buildSelectList('name, "num"') == ['name', 'num']
        qargs.buildSelectList('''[id,'name', "num"]''') == ['id', 'name', "num"]
        //qargs.buildSelectList('{name:"group", amount: "sum"}') == [name: 'group', amount: 'sum']
    }

    def "validate success with q"() {
        when:
        def qjson = "{id: 1, name: 'joe'}"
        def qargs = new QueryArgs().build(q: qjson, sort:"foo:asc")
        qargs.validateQ(true)

        then:
        noExceptionThrown()
        qargs.sort == ['foo':'asc']
        qargs.buildCriteriaMap() == [id: 1, name: 'joe']

    }

    def "validate success with qSearch quick search"() {
        when:
        def qargs = new QueryArgs().build(qSearch:'foo', sort:"foo:asc")
        qargs.validateQ(true)

        then:
        noExceptionThrown()
        qargs.criteriaMap == [$qSearch:'foo']
        qargs.sort == ['foo':'asc']
        qargs.buildCriteriaMap() == ['$qSearch': 'foo']

    }

    def "validateQ fails"() {

        when: 'no q or qsearch'
        QueryArgs qargs = new QueryArgs()
            .build(max: 10, sort:'foo:asc')
            //QueryArgs.of(max: 10, sort:'foo:asc').qRequired(true)
        qargs.validateQ(true)

        then: 'should throw error'
        def ex = thrown(DataProblemException)
        ex.status.code == 418
        //qargs wont even have been built because
        //qargs.buildCriteria() == ['$sort': ['foo':'asc']]

    }

    def "sorts that looks like JSON"() {
        when:
        QueryArgs qargs = QueryArgs.of(sort:'"bar\':"desc" , foo:"asc"')
        Set keys = qargs.sort.keySet()
        then:
        //should keep the order
        keys[0] == "bar"
        keys[1] == "foo"
        qargs.sort == ['foo':'asc', bar:'desc']
        //qargs.buildCriteriaMap() == ['$sort': ['foo':'asc', bar:'desc']]

        when:
        //goofy mixed quotes
        qargs = QueryArgs.of(sort:'{"xxx\': "desc", zzz: "asc"}')
        keys = qargs.sort.keySet()
        then:
        //should keep the order
        keys[0] == "xxx"
        keys[1] == "zzz"
        qargs.sort == ['xxx':'desc', zzz:'asc']
        //qargs.buildCriteriaMap() == ['$sort': ['xxx':'desc', zzz:'asc']]
    }

    def "sorts with only comma separted list of fields"() {
        when:
        QueryArgs qargs = QueryArgs.of(sort:"foo,bar,baz")
        Set keys = qargs.sort.keySet()

        then:"should keep same order and have asc as default"
        //should keep the order
        keys[0] == "foo"
        keys[1] == "bar"
        keys[2] == "baz"
        qargs.sort == ['foo':'asc', bar:'asc', baz:'asc']
        // qargs.buildCriteriaMap() == ['$sort': ['foo':'asc', bar:'asc', baz:'asc']]

        when: "sanity check a single sort"
        qargs = QueryArgs.of(sort:"foo")
        keys = qargs.sort.keySet()
        then:
        //should keep the order
        keys[0] == "foo"
        keys.size() == 1
        qargs.sort == ['foo':'asc']
        //qargs.buildCriteriaMap() == ['$sort': ['foo':'asc']]
    }

    void "defaultSortById"() {
        when:
        QueryArgs qargs = QueryArgs.of().defaultSortById()

        then:

        qargs.sort
        qargs.sort['id'] == 'asc'

        when: "should not apply default Sort, if sort already provided"
        qargs = QueryArgs.of(sort:"foo").defaultSortById()
        Set sortKeys = qargs.sort.keySet()

        then:
        sortKeys
        sortKeys.size() == 1
        sortKeys.contains('foo')
        !sortKeys.contains('id')

        when: "There are projections"
        qargs = QueryArgs.of(projections:["flex.num1":"sum",type:"group"])

        then:
        qargs.projections
        !qargs.sort
    }

    def "parJson sanity check"() {
        when:
        Map res = QueryArgs.parseJson('''{
            str: bar*, num: 1, bool: false, dec: 1.01, dlr: $isNotNull, list: [x,y,z,1,2],
            dot1.dot2.dot3: dot4.dot5,
            'foo': "Bar"
        }''', Map)

        then:
        res == [
            str:"bar*", num:1, bool:false, dec: 1.01, dlr: '$isNotNull', list: ["x","y","z",1,2],
            'dot1.dot2.dot3': 'dot4.dot5',
            "foo": "Bar"
        ]
    }
}
