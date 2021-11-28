/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import gorm.tools.beans.map.EntityIncludesBuilder
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Specification
import yakworks.gorm.testing.model.Enummy
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkExt
import yakworks.gorm.testing.model.SinkItem
import yakworks.gorm.testing.model.Thing

class EntityIncludesBuilderSpec extends Specification implements DataRepoTest {

    void setupSpec() {
        //mockDomain Person
        mockDomains KitchenSink, SinkExt, SinkItem, Thing, Enummy
    }

    void "test buildIncludesMap"(){
        when:
        def res = EntityIncludesBuilder.build("Thing", ['name'])

        then:
        res.className == 'yakworks.gorm.testing.model.Thing'
        res.fields == ['name'] as Set

        when:
        res = EntityIncludesBuilder.build(Thing, null)

        then:
        res.className.contains('Thing') // [className: 'Bookz', props: ['name']]
        res.fields == ['id', 'version', 'name', 'country'] as Set

        when: "check on collections"
        res = EntityIncludesBuilder.build(KitchenSink, ['name', 'items.$*'])

        then:
        res.className.contains('KitchenSink') // [className: 'Bookz', props: ['name']]
        res.fields == ['name', 'items'] as Set
        res.nestedIncludes.size() == 1

        def itemsIncs = res.nestedIncludes['items']
        itemsIncs.className == 'yakworks.gorm.testing.model.SinkItem'
        itemsIncs.fields == ['id', 'name'] as Set

    }

    void "test buildIncludesMap * should return stamp"(){

        when:
        def includes = ['id', 'ext.$*']
        def emapIncs = EntityIncludesBuilder.build(KitchenSink, includes)

        then:
        emapIncs.className == KitchenSink.name // [className: 'Bookz', props: ['name']]
        emapIncs.fields == ['id', 'ext'] as Set
        emapIncs.nestedIncludes.size() == 1

        def extIncs = emapIncs.nestedIncludes['ext']
        extIncs.className == SinkExt.name
        extIncs.fields == ['id', 'name'] as Set

    }

    void "buildIncludesMap for Enum"() {
        when:
        def includes = ['testEnum.*']
        def emapIncs = EntityIncludesBuilder.build(Enummy, includes)

        then:
        emapIncs.className == Enummy.name // [className: 'Bookz', props: ['name']]
        emapIncs.fields == ['testEnum'] as Set
        // shouln not end up with a nested
        emapIncs.nestedIncludes == null

    }

    void "test buildIncludesMap nested *"(){

        when:
        def includes = ['id', 'num', 'ext.*']
        def emapIncs = EntityIncludesBuilder.build(KitchenSink, includes)

        then:
        emapIncs.className == KitchenSink.name // [className: 'Bookz', props: ['name']]
        emapIncs.fields == ['id', 'num', 'ext'] as Set
        emapIncs.nestedIncludes.size() == 1

        def extIncs = emapIncs.nestedIncludes['ext']
        extIncs.className == SinkExt.name
        extIncs.fields == ['id', 'kitchenParent', 'thing', 'version', 'textMax', 'name', 'kitchenSink'] as Set

    }

    void "build with non-existing prop nested"(){

        when:
        def includes = ['id', 'thisShouldBeIgnored', 'thisShouldAlsoBeIgnored.*', 'ext.andThisShouldBeIgnored']
        def emapIncs = EntityIncludesBuilder.build(KitchenSink, includes)

        then:
        emapIncs.className == KitchenSink.name // [className: 'Bookz', props: ['name']]
        //ext will still get added and end up giving the id
        emapIncs.fields == ['id', 'ext'] as Set
        //and no nestedIncludes should get set
        emapIncs.nestedIncludes == null
    }

}
