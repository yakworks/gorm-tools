/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango


import gorm.tools.mango.api.QueryArgs
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.api.problem.data.DataProblemException

class QueryArgsSpec extends Specification {

    def "parseParams q is map"() {
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
        qargs.buildCriteria() == [id: 24, '$sort': ['foo':'asc']]
        qargs.pager

        when: 'q is used and reserved pager is passed'
        qargs = QueryArgs.of(q: [id: 24, offset: 'testing'], max: 10, sort:'foo', page: 2, offset: 10)

        then: 'it should have them in criteria'
        //parsed.criteria.id == 24
        qargs.buildCriteria() == [id: 24, offset: 'testing', '$sort': ['foo':'asc']]

        qargs.pager.max == 10
        qargs.pager.offset == 10
        qargs.pager.page == 2


    }

    def "parseParams q is json"() {

        when: 'q is JSON'
        QueryArgs qargs = QueryArgs.of(q: "{id: 24, something: 'testing'}", sort:'foo:desc' )

        then: 'it should have them in criteria'
        //sort not in the qCriteria
        qargs.qCriteria == [id:24, something: 'testing']
        //sort is in the buildCriteria()
        qargs.buildCriteria() == [id: 24, something: 'testing', '$sort': ['foo':'desc']]

        when: 'not using q and strict=false default'
        //when not using q or criteria
        qargs = QueryArgs.of(name: 'joe', sort:'foo', page: 2)

        then: "adds the params to the criteria"
        qargs.qCriteria == [name: 'joe']
        qargs.buildCriteria() == [name: 'joe', '$sort': ['foo':'asc']]

        qargs.pager.max == 20
        qargs.pager.offset == 20 //(max * (page - 1))
        qargs.pager.page == 2


        when: "strict is false by default"
        qargs = QueryArgs.of(name: 'joe')
        then: "so q param is not required and props will be added"
        qargs.buildCriteria() == [name: 'joe']
        qargs.pager
    }

    def "parseParams when no q and strict=false, just a map"() {

        when: 'not using q'
        //when not using q then it pulls out the sort, order and pager info and leaves the rest as is
        QueryArgs qargs = QueryArgs.of(id: 123, name: 'joe', max: 10, sort:'bar:desc,foo:asc')

        then:
        qargs.buildCriteria() == [id: 123, name: 'joe', '$sort': ['foo':'asc', bar:'desc']]
        qargs.pager.max == 10

        when:
        qargs = QueryArgs.of(name: 'joe')
        then:
        qargs.buildCriteria() == [name: 'joe']
    }

    def "parseParams q is string so its quick search"() {
        when: 'no qSearchFields are passed in params'
        QueryArgs qargs = QueryArgs.of(q:"foo")

        then:
        qargs.buildCriteria() == ['$qSearch': 'foo']
        qargs.pager
    }

    def "parseParams when no q and strict=true"() {

        when:
        QueryArgs qargs = new QueryArgs().strict(true).build(name: 'joe')
        then:
        qargs.qCriteria == [:]
        qargs.buildCriteria() == [:]

        when: 'not using q'
        //when not using q then it pulls out the sort, order and pager info and leaves the rest as is
        qargs = new QueryArgs().strict(true).build(id: 123, name: 'joe', max: 10, sort:'bar')

        then: "the params wont get added to criteria"
        qargs.qCriteria == [:]
        qargs.buildCriteria() == [$sort:[bar:'asc']]
        qargs.pager.max == 10

    }


    def "build with both q and qsearch"() {

        when: 'no qSearchFields'
        def qjson = "{id: 1, name: 'joe'}"
        def qargs = new QueryArgs()
        // qargs.qSearchFields = ['name','num']
        qargs.build(q: qjson, qSearch:'foo')

        then:
        qargs.buildCriteria() == [id: 1, name: 'joe', '$qSearch': 'foo']
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
        qargs.buildCriteria() == [id: 1, name: 'joe', '$sort': ['foo':'asc']]

    }

    def "validate success with qSearch quick search"() {
        when:
        def qargs = new QueryArgs().build(qSearch:'foo', sort:"foo:asc")
        qargs.validateQ(true)

        then:
        noExceptionThrown()
        qargs.qCriteria == [$qSearch:'foo']
        qargs.buildCriteria() == ['$qSearch': 'foo', '$sort': ['foo':'asc']]

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
        qargs.buildCriteria() == ['$sort': ['foo':'asc', bar:'desc']]

        when:
        qargs = QueryArgs.of(sort:'{"xxx\': "desc", zzz: "asc"}')
        keys = qargs.sort.keySet()
        then:
        //should keep the order
        keys[0] == "xxx"
        keys[1] == "zzz"
        qargs.buildCriteria() == ['$sort': ['xxx':'desc', zzz:'asc']]
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
        qargs.buildCriteria() == ['$sort': ['foo':'asc', bar:'asc', baz:'asc']]

        when: "sanity check a single sort"
        qargs = QueryArgs.of(sort:"foo")
        keys = qargs.sort.keySet()
        then:
        //should keep the order
        keys[0] == "foo"
        keys.size() == 1
        qargs.buildCriteria() == ['$sort': ['foo':'asc']]
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
