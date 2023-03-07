/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import org.springframework.security.core.userdetails.UsernameNotFoundException

import gorm.tools.mango.api.QueryArgs
import gorm.tools.problem.ValidationProblem
import spock.lang.Specification
import yakworks.api.problem.data.DataProblemException

class QueryArgsSpec extends Specification {

    def "parseParams q is map"() {
        when:
        QueryArgs qargs = QueryArgs.of(q: [id: 24], max: 10, sort:'foo', page: 2, offset: 10)
        then:
        //parsed.criteria.id == 24
        qargs.criteria == [id: 24, '$sort': ['foo':'asc']]
        qargs.pager

        when: 'q is used and reserved pager is passed'
        qargs = QueryArgs.of(q: [id: 24, offset: 'testing'], max: 10, sort:'foo', page: 2, offset: 10)

        then: 'it should have them in criteria'
        //parsed.criteria.id == 24
        qargs.criteria == [id: 24, offset: 'testing', '$sort': ['foo':'asc']]

        qargs.pager.max == 10
        qargs.pager.offset == 10
        qargs.pager.page == 2


    }

    def "parseParams q is json"() {

        when: 'q is JSON'
        QueryArgs qargs = QueryArgs.of(q: "{id: 24, something: 'testing'}", sort:'foo:desc' )

        then: 'it should have them in criteria'
        //parsed.criteria.id == 24
        qargs.criteria == [id: 24, something: 'testing', '$sort': ['foo':'desc']]

        when: 'not using q'
        //when not using q or criteria t
        qargs = QueryArgs.of(name: 'joe', sort:'foo', page: 2)

        then:
        qargs.criteria == [name: 'joe', '$sort': ['foo':'asc']]

        qargs.pager.max == 20
        qargs.pager.offset == 20 //(max * (page - 1))
        qargs.pager.page == 2


        when:
        qargs = QueryArgs.of(name: 'joe')
        then:
        qargs.criteria == [name: 'joe']
        qargs.pager
    }

    def "parseParams no q, just a map"() {

        when: 'not using q'
        //when not using q then it pulls out the sort, order and pager info and leaves the rest as is
        QueryArgs qargs = QueryArgs.of(id: 123, name: 'joe', max: 10, sort:'foo:asc,bar:desc')

        then:
        qargs.criteria == [id: 123, name: 'joe', '$sort': ['foo':'asc', bar:'desc']]
        qargs.pager.max == 10

        when:
        qargs = QueryArgs.of(name: 'joe')
        then:
        qargs.criteria == [name: 'joe']
    }

    def "parseParams q is string so its quick search"() {
        when: 'no qSearchFields are passed in params'
        QueryArgs qargs = QueryArgs.of(q:"foo")

        then:
        qargs.criteria == ['$qSearch': 'foo']
        qargs.pager
    }

    def "build with both q and qsearch"() {

        when: 'no qSearchFields'
        def qjson = "{id: 1, name: 'joe'}"
        def qargs = new QueryArgs()
        // qargs.qSearchFields = ['name','num']
        qargs.build(q: qjson, qSearch:'foo')

        then:
        qargs.criteria == [id: 1, name: 'joe', '$qSearch': 'foo']
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

    def "validate success with q"() {
        when:
        def qjson = "{id: 1, name: 'joe'}"
        def qargs = new QueryArgs(qRequired: true).build(q: qjson, sort:"foo:asc")
        qargs.validateQ()

        then:
        noExceptionThrown()
        qargs.criteria == [id: 1, name: 'joe', '$sort': ['foo':'asc']]

    }

    def "validate success with qSearch quick search"() {
        when:
        def qargs = new QueryArgs(qRequired: true).build(qSearch:'foo', sort:"foo:asc")
        qargs.validateQ()

        then:
        noExceptionThrown()
        qargs.criteria == ['$qSearch': 'foo', '$sort': ['foo':'asc']]

    }

    def "validateQ fails"() {

        when: 'no q or qsearch'
        QueryArgs qargs = new QueryArgs(qRequired: true).build(max: 10, sort:'foo:asc')
            //QueryArgs.of(max: 10, sort:'foo:asc').qRequired(true)
        qargs.validateQ()

        then: 'should throw error'
        def ex = thrown(DataProblemException)
        ex.status.code == 418
        qargs.criteria == ['$sort': ['foo':'asc']]

    }


}
