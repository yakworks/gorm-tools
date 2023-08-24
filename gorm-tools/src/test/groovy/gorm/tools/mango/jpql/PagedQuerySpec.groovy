/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.jpql

import spock.lang.Specification
import yakworks.commons.map.LazyPathKeyMap
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

class PagedQuerySpec extends Specification implements GormHibernateTest  {
    static List entityClasses = [KitchenSink, SinkItem]

    void setupSpec() {
        KitchenSink.createKitchenSinks(10)
    }

    void cleanupSpec() {
        KitchenSink.cleanup()
    }

    void "sanity check"() {
        expect:
        10 == KitchenSink.count()
    }

    void "count works"() {
        when:
        def staticApi = KitchenSink.repo.gormStaticApi()
        def qe = new PagedQuery(staticApi)
        int count = qe.countQuery("Select id from KitchenSink", [:])

        then:
        count == 10
    }

    void "list returns pagedList with the right total"() {
        when:
        def staticApi = KitchenSink.repo.gormStaticApi()
        def qe = new PagedQuery(staticApi)
        List list = qe.list("Select id from KitchenSink", [:], [max: 2])

        then:
        list.totalCount == 10
        list.size() == 2
    }

    void "list with HQL projections"() {
        when:"A jpa query is built"

        String expectSql = """
            SELECT SUM(kitchenSink.amount) as amount_sum, SUM(kitchenSink.sinkLink.amount) as sinkLink_amount_sum,
            kitchenSink.kind as kind, kitchenSink.thing.country as thing_country
            FROM yakworks.testing.gorm.model.KitchenSink AS kitchenSink
            LEFT JOIN kitchenSink.sinkLink
            GROUP BY kitchenSink.kind,kitchenSink.thing.country
        """
        List<Map> list = doList(expectSql, [:], [:], ['amount_sum', 'sinkLink_amount_sum'])

        then:"The query is valid"

        list.size() == 3
        list.totalCount == 3

        LazyPathKeyMap row1 = list[0]
        //row1.init()
        //Map row1 = row.cloneMap()
        row1.keySet() == ['amount', 'thing', 'kind', 'sinkLink'] as Set
        row1.containsKey('thing')
        !row1.containsKey('thing.country')
        row1['thing'].containsKey('country')
        row1.containsKey('sinkLink')
        row1['sinkLink'].containsKey('amount')
        row1.containsKey('amount')
        row1.containsKey('kind')
        row1.thing.country == 'US'
        row1.amount == 30.00
        row1.kind == KitchenSink.Kind.CLIENT
    }


    List doList(String query, Map params, Map args, List<String> systemAliases = []){
        def staticApi = KitchenSink.repo.gormStaticApi()
        //SimplePagedQuery spq = new SimplePagedQuery(staticApi)
        PagedQuery spq = new PagedQuery(staticApi, systemAliases)
        def list = spq.list(query, params, args)
        return list
    }
}
