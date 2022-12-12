/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.KitchenSinkRepo
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

/**
 * QueryMangoEntityApi is a trait on GromRepo. so we test through that
 */
class QueryMangoEntityApiSpec extends Specification implements GormHibernateTest {
    static List entityClasses = [KitchenSink, SinkItem]

    @Autowired KitchenSinkRepo kitchenSinkRepo

    void setupSpec() {
        KitchenSink.createKitchenSinks(5)
    }

    void setup(){
        assert 5 == KitchenSink.count()
    }

    void cleanupSpec() {
        KitchenSink.cleanup()
    }

    void "exists works"() {
        expect:
        kitchenSinkRepo.exists(1L)
        kitchenSinkRepo.exists(5L)
        !kitchenSinkRepo.exists(-99L)
    }
}
