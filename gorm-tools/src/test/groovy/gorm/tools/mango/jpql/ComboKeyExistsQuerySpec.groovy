/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.jpql


import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.model.Thing
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

    void "exists association fields id"() {
        when:
        def qe = ComboKeyExistsQuery.of(KitchenSink).keyNames(['id','thingId'])

        then:
        qe.exists([id: 1L, 'thingId': 1L])
        !qe.exists([id: 1L, 'thingId': 2L])
    }

    void "exists association fields dot id"() {
        when:
        def qe = ComboKeyExistsQuery.of(KitchenSink).keyNames(['id','thing.id'])

        then:
        qe.exists([id: 1L, 'thing.id': 1L])
    }

    void "exists single field"() {
        when:
        def qe = ComboKeyExistsQuery.of(KitchenSink).keyNames(['id'])

        then:
        qe.exists([id: 1L])
        !qe.exists([id: -999L])
    }

    void "exists single field as domain"() {
        when:
        def qe = ComboKeyExistsQuery.of(KitchenSink).keyNames(['thing'])
        def thing1 = Thing.get(1)

        then:
        qe.exists([thing: thing1])

        !qe.exists([thing: Thing.load(-999L)])
    }

}
