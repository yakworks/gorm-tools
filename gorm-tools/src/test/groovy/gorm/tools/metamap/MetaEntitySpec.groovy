/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap


import yakworks.gorm.testing.unit.DataRepoTest
import spock.lang.Specification
import yakworks.gorm.testing.model.Enummy
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkExt
import yakworks.gorm.testing.model.SinkItem
import yakworks.gorm.testing.model.Thing
import yakworks.meta.MetaEntity
import yakworks.meta.MetaProp

class MetaEntitySpec extends Specification implements DataRepoTest {

    void setupSpec() {
        //mockDomain Person
        mockDomains KitchenSink, SinkExt, SinkItem, Thing, Enummy
    }

    void "test equals"() {
        when:
        def mmi1 = new MetaEntity(KitchenSink)
        def mmi2 = new MetaEntity(KitchenSink)

        then:
        mmi1 == mmi2

        when:
        mmi1 = MetaGormEntityBuilder.build(KitchenSink, ['name', 'items.$*'])
        mmi2 = MetaGormEntityBuilder.build(KitchenSink, ['name', 'items.$*'])

        then:
        mmi1 == mmi2
    }

    void "test toBasicMap"(){
        when:
        //simple
        def includes = ['id', 'num', 'name', 'ext.thing.name' ]
        MetaEntity mmi = MetaGormEntityBuilder.build(KitchenSink, includes)
        Map basicMap = mmi.toMap()

        then:
        basicMap.size() == 4
        basicMap.keySet() == ['id', 'num', 'name', 'ext'] as Set
        basicMap.ext.size() == 1
        basicMap.ext.keySet() == ['thing'] as Set
        basicMap.ext.thing.size() == 1
        basicMap.ext.thing.keySet() == ['name'] as Set
    }

    void "test flatten"(){
        when:
        //simple
        def includes = ['id', 'num', 'amount', 'ext.thing.name']
        def expectedIncludes = includes
        MetaEntity mmi = MetaGormEntityBuilder.build(KitchenSink, includes)
        def flatMap = mmi.flatten()

        then:
        //in this case it should equal the passes in includes
        flatMap.keySet() == expectedIncludes as Set
        flatMap['id'] instanceof MetaProp
        flatMap['id'].classType == Long
    }

    void "test flattenProps"(){
        when:
        //simple
        def includes = ['id', 'num', 'name', 'ext.thing.name']
        def expectedIncludes = includes
        MetaEntity mmi = MetaGormEntityBuilder.build(KitchenSink, includes)
        Set props = mmi.flattenProps()

        then:
        //in this case it should equal the passes in includes
        props == expectedIncludes as Set

        when:
        //simple
        includes = ['id', 'thing.$*']
        expectedIncludes = ['id', 'thing.id', 'thing.name']
        props = MetaGormEntityBuilder.build(KitchenSink, includes).flattenProps()

        then:
        props == expectedIncludes as Set
    }

}
