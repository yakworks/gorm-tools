/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import gorm.tools.mango.jpql.SimplePagedQuery
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.testing.spring.AutowiredTest
import spock.lang.Shared
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.KitchenSinkRepo
import yakworks.gorm.testing.model.SinkItem

class SimplePagedQuerySpec extends GormToolsHibernateSpec implements AutowiredTest {

    DefaultMangoQuery mangoQuery
    @Shared KitchenSinkRepo kitchenSinkRepo

    List<Class> getDomainClasses() { [KitchenSink, SinkItem] }

    void setupSpec() {
        kitchenSinkRepo = KitchenSink.repo
        kitchenSinkRepo.createKitchenSinks(10)
    }

    void cleanupSpec() {
        kitchenSinkRepo.deleteAll()
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
