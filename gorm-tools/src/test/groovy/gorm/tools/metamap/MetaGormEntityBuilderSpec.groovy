/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap


import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Specification
import yakworks.gorm.testing.model.Enummy
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkExt
import yakworks.gorm.testing.model.SinkItem
import yakworks.gorm.testing.model.Thing
import yakworks.meta.MetaEntity

class MetaGormEntityBuilderSpec extends Specification implements DataRepoTest {

    void setupSpec() {
        //mockDomain Person
        mockDomains KitchenSink, SinkExt, SinkItem, Thing, Enummy
    }

    void "KitchenSink *"(){
        when:
        //getCustom should be setup in the config
        def includes = ['*']
        MetaEntity emapIncs = MetaGormEntityBuilder.build(KitchenSink, includes)

        then:
        //should be 23 of them.
        emapIncs.metaProps.size() == 23
    }

    void "build works with short entity name and fully qualified"(){
        when:
        def res = MetaGormEntityBuilder.build("Thing", ['name'])

        then:
        res.className == 'yakworks.gorm.testing.model.Thing'

        when: "use fully qualified"
        res = MetaGormEntityBuilder.build('yakworks.gorm.testing.model.Thing', ['name'])

        then:
        res.className == 'yakworks.gorm.testing.model.Thing'
    }

    void "test buildIncludesMap"(){
        when:
        def res = MetaGormEntityBuilder.build("Thing", ['name'])

        then:
        res.className == 'yakworks.gorm.testing.model.Thing'
        res.shortClassName == 'Thing'
        res.metaProps.keySet() == ['name'] as Set

        when:
        res = MetaGormEntityBuilder.build(Thing, null)

        then:
        res.className.contains('Thing') // [className: 'Bookz', props: ['name']]
        res.metaProps.keySet() == ['id', 'version', 'name', 'country'] as Set

        when: "check on collections"
        res = MetaGormEntityBuilder.build(KitchenSink, ['name', 'items.$*'])

        then:
        res.className.contains('KitchenSink') // [className: 'Bookz', props: ['name']]
        res.metaProps.keySet() == ['name', 'items'] as Set
        res.metaEntityProps.size() == 1

        def itemsIncs = res.metaEntityProps['items']
        itemsIncs.className == 'yakworks.gorm.testing.model.SinkItem'
        itemsIncs.shortClassName == 'SinkItem'
        itemsIncs.metaProps.keySet() == ['id', 'name'] as Set

    }

    void "test buildIncludesMap * should return stamp"(){

        when:
        def includes = ['id', 'ext.$*']
        def emapIncs = MetaGormEntityBuilder.build(KitchenSink, includes)

        then:
        emapIncs.className == KitchenSink.name // [className: 'Bookz', props: ['name']]
        emapIncs.shortClassName == 'KitchenSink'
        emapIncs.metaProps.keySet() == ['id', 'ext'] as Set
        emapIncs.metaEntityProps.size() == 1

        def extIncs = emapIncs.metaEntityProps['ext']
        extIncs.className == SinkExt.name
        extIncs.shortClassName == 'SinkExt'
        extIncs.metaProps.keySet() == ['id', 'name'] as Set

    }

    void "buildIncludesMap for Enum"() {
        when:
        def includes = ['testEnum.*']
        def emapIncs = MetaGormEntityBuilder.build(Enummy, includes)

        then:
        emapIncs.className == Enummy.name // [className: 'Bookz', props: ['name']]
        emapIncs.metaProps.keySet() == ['testEnum'] as Set
        // shouln not end up with a nested
        emapIncs.metaEntityProps.isEmpty()

    }

    void "build with nested *"(){

        when:
        def includes = ['id', 'num', 'ext.*']
        def emapIncs = MetaGormEntityBuilder.build(KitchenSink, includes)

        then:
        emapIncs.className == KitchenSink.name // [className: 'Bookz', props: ['name']]
        emapIncs.metaProps.keySet() == ['id', 'num', 'ext'] as Set
        emapIncs.metaEntityProps.size() == 1

        def extIncs = emapIncs.metaEntityProps['ext']
        extIncs.className == SinkExt.name
        extIncs.metaProps.keySet() == ['id', 'kitchenParent', 'thing', 'version', 'textMax', 'name'] as Set

    }

    void "build with non-existing prop nested"(){

        when:
        def includes = ['id', 'thisShouldBeIgnored', 'thisShouldAlsoBeIgnored.*', 'ext.andThisShouldBeIgnored']
        def emapIncs = MetaGormEntityBuilder.build(KitchenSink, includes)

        then:
        emapIncs.className == KitchenSink.name // [className: 'Bookz', props: ['name']]
        //ext will still get added and end up giving the id
        emapIncs.metaProps.keySet() == ['id', 'ext'] as Set
        //and no metaEntityProps should get set
        emapIncs.metaEntityProps.isEmpty()
    }

    void "build nested in nested"(){

        when:
        //getCustom should be setup in the config
        def includes = ['id', 'ext.id', 'ext.thing.id']
        def emapIncs = MetaGormEntityBuilder.build(KitchenSink, includes)

        then:
        emapIncs.metaProps.keySet() == ['id', 'ext'] as Set
        emapIncs.metaEntityProps.size() == 1
        //will have nested includes
        def extIncs = emapIncs.metaEntityProps['ext']
        extIncs.className == SinkExt.name
        extIncs.metaProps.keySet() == ['id', 'thing'] as Set

        def thingLevel = extIncs.metaEntityProps['thing']
        thingLevel.metaProps.keySet() == ['id'] as Set
    }

    void "build with customer includes key"(){

        when:
        //getCustom should be setup in the config
        def includes = ['id', 'ext.$getCustom']
        def emapIncs = MetaGormEntityBuilder.build(KitchenSink, includes)

        then:
        emapIncs.metaProps.keySet() == ['id', 'ext'] as Set
        emapIncs.metaEntityProps.size() == 1
        //will have nested includes
        def extIncs = emapIncs.metaEntityProps['ext']
        extIncs.className == SinkExt.name
        extIncs.metaProps.keySet() == ['id', 'name', 'thing'] as Set

        def thingLevel = extIncs.metaEntityProps['thing']
        thingLevel.metaProps.keySet() == ['id', 'name'] as Set
    }

}
