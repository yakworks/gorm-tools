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

class MetaMapIncludesBuilderSpec extends Specification implements DataRepoTest {

    void setupSpec() {
        //mockDomain Person
        mockDomains KitchenSink, SinkExt, SinkItem, Thing, Enummy
    }

    void "KitchenSink *"(){
        when:
        //getCustom should be setup in the config
        def includes = ['*']
        MetaMapIncludes emapIncs = MetaMapIncludesBuilder.build(KitchenSink, includes)

        then:
        //should be 23 of them.
        emapIncs.propsMap.size() == 23
    }

    void "build works with short entity name and fully qualified"(){
        when:
        def res = MetaMapIncludesBuilder.build("Thing", ['name'])

        then:
        res.className == 'yakworks.gorm.testing.model.Thing'

        when: "use fully qualified"
        res = MetaMapIncludesBuilder.build('yakworks.gorm.testing.model.Thing', ['name'])

        then:
        res.className == 'yakworks.gorm.testing.model.Thing'
    }

    void "test buildIncludesMap"(){
        when:
        def res = MetaMapIncludesBuilder.build("Thing", ['name'])

        then:
        res.className == 'yakworks.gorm.testing.model.Thing'
        res.shortClassName == 'Thing'
        res.propsMap.keySet() == ['name'] as Set

        when:
        res = MetaMapIncludesBuilder.build(Thing, null)

        then:
        res.className.contains('Thing') // [className: 'Bookz', props: ['name']]
        res.propsMap.keySet() == ['id', 'version', 'name', 'country'] as Set

        when: "check on collections"
        res = MetaMapIncludesBuilder.build(KitchenSink, ['name', 'items.$*'])

        then:
        res.className.contains('KitchenSink') // [className: 'Bookz', props: ['name']]
        res.propsMap.keySet() == ['name', 'items'] as Set
        res.nestedIncludes.size() == 1

        def itemsIncs = res.nestedIncludes['items']
        itemsIncs.className == 'yakworks.gorm.testing.model.SinkItem'
        itemsIncs.shortClassName == 'SinkItem'
        itemsIncs.propsMap.keySet() == ['id', 'name'] as Set

    }

    void "test buildIncludesMap * should return stamp"(){

        when:
        def includes = ['id', 'ext.$*']
        def emapIncs = MetaMapIncludesBuilder.build(KitchenSink, includes)

        then:
        emapIncs.className == KitchenSink.name // [className: 'Bookz', props: ['name']]
        emapIncs.shortClassName == 'KitchenSink'
        emapIncs.propsMap.keySet() == ['id', 'ext'] as Set
        emapIncs.nestedIncludes.size() == 1

        def extIncs = emapIncs.nestedIncludes['ext']
        extIncs.className == SinkExt.name
        extIncs.shortClassName == 'SinkExt'
        extIncs.propsMap.keySet() == ['id', 'name'] as Set

    }

    void "buildIncludesMap for Enum"() {
        when:
        def includes = ['testEnum.*']
        def emapIncs = MetaMapIncludesBuilder.build(Enummy, includes)

        then:
        emapIncs.className == Enummy.name // [className: 'Bookz', props: ['name']]
        emapIncs.propsMap.keySet() == ['testEnum'] as Set
        // shouln not end up with a nested
        emapIncs.nestedIncludes.isEmpty()

    }

    void "build with nested *"(){

        when:
        def includes = ['id', 'num', 'ext.*']
        def emapIncs = MetaMapIncludesBuilder.build(KitchenSink, includes)

        then:
        emapIncs.className == KitchenSink.name // [className: 'Bookz', props: ['name']]
        emapIncs.propsMap.keySet() == ['id', 'num', 'ext'] as Set
        emapIncs.nestedIncludes.size() == 1

        def extIncs = emapIncs.nestedIncludes['ext']
        extIncs.className == SinkExt.name
        extIncs.propsMap.keySet() == ['id', 'kitchenParent', 'thing', 'version', 'textMax', 'name', 'kitchenSink'] as Set

    }

    void "build with non-existing prop nested"(){

        when:
        def includes = ['id', 'thisShouldBeIgnored', 'thisShouldAlsoBeIgnored.*', 'ext.andThisShouldBeIgnored']
        def emapIncs = MetaMapIncludesBuilder.build(KitchenSink, includes)

        then:
        emapIncs.className == KitchenSink.name // [className: 'Bookz', props: ['name']]
        //ext will still get added and end up giving the id
        emapIncs.propsMap.keySet() == ['id', 'ext'] as Set
        //and no nestedIncludes should get set
        emapIncs.nestedIncludes.isEmpty()
    }

    void "build nested in nested"(){

        when:
        //getCustom should be setup in the config
        def includes = ['id', 'ext.id', 'ext.thing.id']
        def emapIncs = MetaMapIncludesBuilder.build(KitchenSink, includes)

        then:
        emapIncs.propsMap.keySet() == ['id', 'ext'] as Set
        emapIncs.nestedIncludes.size() == 1
        //will have nested includes
        def extIncs = emapIncs.nestedIncludes['ext']
        extIncs.className == SinkExt.name
        extIncs.propsMap.keySet() == ['id', 'thing'] as Set

        def thingLevel = extIncs.nestedIncludes['thing']
        thingLevel.propsMap.keySet() == ['id'] as Set
    }

    void "build with customer includes key"(){

        when:
        //getCustom should be setup in the config
        def includes = ['id', 'ext.$getCustom']
        def emapIncs = MetaMapIncludesBuilder.build(KitchenSink, includes)

        then:
        emapIncs.propsMap.keySet() == ['id', 'ext'] as Set
        emapIncs.nestedIncludes.size() == 1
        //will have nested includes
        def extIncs = emapIncs.nestedIncludes['ext']
        extIncs.className == SinkExt.name
        extIncs.propsMap.keySet() == ['id', 'name', 'thing'] as Set

        def thingLevel = extIncs.nestedIncludes['thing']
        thingLevel.propsMap.keySet() == ['id', 'name'] as Set
    }

}
