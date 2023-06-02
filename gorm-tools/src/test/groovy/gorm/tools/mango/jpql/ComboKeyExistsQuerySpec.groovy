/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.jpql


import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

class ComboKeyExistsQuerySpec extends Specification implements GormHibernateTest  {
    static List entityClasses = [KitchenSink, SinkItem]

    void setupSpec() {
        KitchenSink.createKitchenSinks(5)
    }

    void cleanupSpec() {
        KitchenSink.cleanup()
    }

    void "sanity check"() {
        expect:
        5 == KitchenSink.count()
    }

    void "exists with multiple fields"() {
        when:
        def qe = ComboKeyExistsQuery.of(KitchenSink).keyNames(['id','name2'])

        then:
        qe.exists([id: 1L, name2: 'KitchenSink-1'])
        qe.queryString == "select 1 from yakworks.testing.gorm.model.KitchenSink where id = :idVal AND name2 = :name2Val"

        !qe.exists([id: 1L, name2: 'foo'])
    }

    void "exists size mismatch"() {
        when:
        def qe = ComboKeyExistsQuery.of(KitchenSink).keyNames(['id','name2'])
        qe.exists([id: 1L, name2: 'KitchenSink-1', num: 'foo'])
        then:
        IllegalArgumentException ex = thrown()
    }

}
