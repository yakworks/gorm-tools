/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.jpql

import gorm.tools.mango.jpql.SimplePagedQuery
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

class SimplePagedQuerySpec extends Specification implements GormHibernateTest  {
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
        def qe = new SimplePagedQuery(staticApi)
        int count = qe.countQuery("Select id from KitchenSink", [:])

        then:
        count == 10
    }

    void "list returns pagedList with the right total"() {
        when:
        def staticApi = KitchenSink.repo.gormStaticApi()
        def qe = new SimplePagedQuery(staticApi)
        List list = qe.list("Select id from KitchenSink", [:], [max: 2])

        then:
        list.totalCount == 10
        list.size() == 2
    }

}
