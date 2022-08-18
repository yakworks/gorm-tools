/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap.services

import yakworks.meta.MetaMap
import yakworks.meta.MetaMapList
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Specification
import yakworks.gorm.testing.model.Enummy
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkExt
import yakworks.gorm.testing.model.SinkItem
import yakworks.gorm.testing.model.TestEnum
import yakworks.gorm.testing.model.TestEnumIdent
import yakworks.gorm.testing.model.Thing

class MetaMapServiceSpec extends Specification implements DataRepoTest {

    MetaMapService metaMapService = new MetaMapService(
        metaMapIncludesService: new MetaMapIncludesService()
    )

    void setupSpec() {
        //mockDomain Person
        mockDomains KitchenSink, SinkExt, SinkItem, Thing, Enummy
    }

    void "createEntityMap with includes"() {
        when: 'sanity check'
        MetaMap emap = metaMapService.createMetaMap(KitchenSink.build(1), ['id', 'num', 'ext.id'])

        then:
        3 == emap.size()
        emap.getIncludes() == ['id', 'num', 'ext'] as Set
    }

    void "test null assoc"() {
        when:
        def ks = KitchenSink.build(1)
        def result = metaMapService.createMetaMap(ks, ['id', 'sinkLink.thing.name'])

        then:
        result.size() == 2
        result == [ id: 1, sinkLink: null ]
    }

    void "createEntityMap dingus on KitchenSink"() {
        setup:
        def ks = KitchenSink.build(1)
        expect:
        result == metaMapService.createMetaMap(ks, fields) //BeanPathTools.buildMapFromPaths(book, fields)

        where:
        fields                        | result
        ['num', 'name']               | [num: '1', name: 'Sink1']
        ['name', 'simplePogo.foo']    | [name: 'Sink1', simplePogo: [ foo: 'fly']]
        ['name', 'items.name']        | [ name: 'Sink1', items: [[ name: 'red'], [ name: 'blue']] ]
    }

    void "createEntityMap enums on kitchenSink"() {
        when:
        def ks = KitchenSink.build(1)
        def result = metaMapService.createMetaMap(ks, ['num', 'kind', 'status', 'thing.name'])

        then:
        result == [num: '1', kind: 'VENDOR', status:[id:2, name:'Inactive'], thing: [name: 'Thing1']]
    }

    void "works with space in field and its null"() {
        when: 'a field has spaces'
        def ks = KitchenSink.build(1)
        def result = metaMapService.createMetaMap(ks, ['num', '  simplePogo.foo'])

        then: 'its should trim them and still work'
        result == [num: '1', simplePogo: [ foo: 'fly']]

    }

    void "test buildMapFromPaths Enum"() {
        setup:
        Enummy et = new Enummy(
            testEnum: TestEnum.FOO,
            enumIdent: TestEnumIdent.Num2
        )

        expect:
        exp == metaMapService.createMetaMap(et, path)

        where:

        path                                | exp
        ['enumIdent.*']                     | [enumIdent:[id:2, name:'Num2']]
        ['enumIdent.id', 'enumIdent.num']   | [enumIdent:[id:2, num:'2-Num2']]
        ['testEnum', 'enumIdent']           | [testEnum:'FOO', enumIdent:[id:2, name:'Num2']]

    }

    void "test createEntityBeanMap with EnumThing list"() {
        when:
        List enummyList = []
        (1..2).each{id ->
            def et = new Enummy(
                id: id,
                testEnum: TestEnum.FOO,
                enumIdent: TestEnumIdent.Num2
            )
            enummyList.add(et)
        }
        def resultsList = metaMapService.createMetaMapList(enummyList, ['*'])

        then:
        resultsList.size() == 2
        resultsList[0] == [id: 1, testEnum: 'FOO', version:null, enumIdent: [id:2, name:'Num2']]
        resultsList[1] == [id: 2, testEnum: 'FOO', version:null, enumIdent: [id:2, name:'Num2']]

        when:
        resultsList = metaMapService.createMetaMapList(enummyList, ['testEnum', 'enumIdent'])

        then:
        resultsList[0] == [testEnum: 'FOO', enumIdent: [id:2, name:'Num2']]
        resultsList[1] == [testEnum: 'FOO', enumIdent: [id:2, name:'Num2']]

    }

    void "association tests"() {
        setup:
        def ks = KitchenSink.build(1)
        ks.sinkLink = ks
        ks.stringList = ['red', 'blue', 'green']

        expect:
        exp == metaMapService.createMetaMap(ks, path)

        where:
        path                          | exp
        ['thing.$*']                  | [thing: [id:1, name:'Thing1']]
        ['thing.$stamp']              | [thing: [id:1, name:'Thing1']]
        ['thing.*']                   | [thing: [id:1, version:0, name:'Thing1', country:'US']]
        ['ext.name']                  | [ext: [name: 'SinkExt1']]
        ['sinkLink.ext.name']         | [sinkLink: [ext: [name: 'SinkExt1']]]
        ['bazMap', 'stringList']      | [bazMap: [foo: 'bar'], stringList:['red', 'blue', 'green']]
    }

    void "non association list should be wrapped too"() {
        when:
        def ks = KitchenSink.build(1)
        def emap = metaMapService.createMetaMap(ks, ['id', 'items'])

        then:
        ks.items
        emap.items
        emap.items instanceof MetaMapList

        when:
        def items = emap.items as MetaMapList

        then:
        items[0].keySet() == ['id'] as Set
    }

    void "with named includes key"() {
        when:
        def ks = KitchenSink.build(1)
        ks.ext.thing = new Thing(id: 99, name: "Thing99").persist()

        def emap = metaMapService.createMetaMap(ks, ['id', 'ext.$getCustom'])

        then:
        emap == [id: 1, ext: [id:1, name:'SinkExt1', thing: [id: 99, name: 'Thing99']]]

    }

}
