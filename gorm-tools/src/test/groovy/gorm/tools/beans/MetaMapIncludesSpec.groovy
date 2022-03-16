/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import gorm.tools.beans.map.MetaMapIncludes
import gorm.tools.beans.map.MetaMapIncludesBuilder
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Specification
import yakworks.gorm.testing.model.Enummy
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkExt
import yakworks.gorm.testing.model.SinkItem
import yakworks.gorm.testing.model.Thing

class MetaMapIncludesSpec extends Specification implements DataRepoTest {

    void setupSpec() {
        //mockDomain Person
        mockDomains KitchenSink, SinkExt, SinkItem, Thing, Enummy
    }

    void "test equals"() {
        when:
        def mmi1 = new MetaMapIncludes("Foo")
        def mmi2 = new MetaMapIncludes("Foo")

        then:
        mmi1 == mmi2

        when:
        mmi1 = MetaMapIncludesBuilder.build(KitchenSink, ['name', 'items.$*'])
        mmi2 = MetaMapIncludesBuilder.build(KitchenSink, ['name', 'items.$*'])

        then:
        mmi1 == mmi2
    }
}
