/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.jpql


import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

class KeyExistsQuerySpec extends Specification implements GormHibernateTest  {
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

    void "exists"() {
        when:
        def qe = KeyExistsQuery.of(KitchenSink)

        then:
        qe.exists(1L)
        qe.queryString == "select id from yakworks.testing.gorm.model.KitchenSink where id = :keyVal"

        !qe.exists(-999L)
    }

}
