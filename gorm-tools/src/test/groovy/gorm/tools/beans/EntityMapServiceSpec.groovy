/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans


import gorm.tools.beans.domain.BookAuthor
import gorm.tools.beans.domain.BookTag
import gorm.tools.beans.domain.Bookz
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.gorm.testing.model.Enummy
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkExt
import yakworks.gorm.testing.model.SinkItem
import yakworks.gorm.testing.model.SinkStatus
import yakworks.gorm.testing.model.TestEnum
import yakworks.gorm.testing.model.TestEnumIdent
import yakworks.gorm.testing.model.Thing

class EntityMapServiceSpec extends Specification implements DataRepoTest {

    EntityMapService entityMapService = new EntityMapService()

    void setupSpec() {
        //mockDomain Person
        mockDomains KitchenSink, SinkExt, SinkItem, Thing, Enummy
    }

    void "test buildIncludesMap"(){
        when:
        def res = entityMapService.buildIncludesMap("Thing", ['name'])

        then:
        res.className == 'yakworks.gorm.testing.model.Thing'
        res.fields == ['name'] as Set

        when:
        res = entityMapService.buildIncludesMap(Thing)

        then:
        res.className.contains('Thing') // [className: 'Bookz', props: ['name']]
        res.fields == ['id', 'version', 'name', 'country'] as Set

        when: "check on collections"
        res = entityMapService.buildIncludesMap(KitchenSink, ['name', 'items.*'])

        then:
        res.className.contains('KitchenSink') // [className: 'Bookz', props: ['name']]
        res.fields == ['name', 'items'] as Set
        res.nestedIncludes.size() == 1
        res.nestedIncludes['items'].with{
            className == 'yakworks.gorm.testing.model.SinkItem'
            fields == ['id', 'version', 'name'] as Set
        }

        when:
        res = entityMapService.buildIncludesMap(KitchenSink, ['id', 'num', 'ext.*', 'sinkLink.id', 'sinkLink.num'])

        then:
        res.className == KitchenSink.name // [className: 'Bookz', props: ['name']]
        res.fields == ['id', 'num', 'ext', 'sinkLink'] as Set
        res.nestedIncludes.size() == 2
        res.nestedIncludes['ext'].with{
            className == SinkExt.name
            fields == ['id', 'version', 'name'] as Set
        }
        res.nestedIncludes['sinkLink'].with{
            className == KitchenSink.name
            fields == ['id', 'version', 'name'] as Set
        }
    }

    void "entityMap getIncludes()"() {
        when: 'sanity check'
        def emap = entityMapService.createEntityMap(KitchenSink.build(1), ['id', 'num', 'ext.id'])

        then:
        3 == emap.size()
        emap.getIncludes() == ['id', 'num', 'ext'] as Set
    }

    void "test null assoc"() {
        when:
        def ks = KitchenSink.build(1)
        def result = entityMapService.createEntityMap(ks, ['id', 'sinkLink.thing.name'])

        then:
        result.size() == 2
        result == [ id: 1, sinkLink: null ]
    }

    void "createEntityMap dingus on KitchenSink"() {
        setup:
        def ks = KitchenSink.build(1)
        expect:
        result == entityMapService.createEntityMap(ks, fields) //BeanPathTools.buildMapFromPaths(book, fields)

        where:
        fields                        | result
        ['num', 'name']               | [num: '1', name: 'Sink1']
        ['name', 'simplePogo.foo']    | [name: 'Sink1', simplePogo: [ foo: 'fly']]
        ['name', 'items.name']        | [ name: 'Sink1', items: [[ name: 'red'], [ name: 'blue']] ]
    }

    void "createEntityMap enums on kitchenSink"() {
        when:
        def ks = KitchenSink.build(1)
        def result = entityMapService.createEntityMap(ks, ['num', 'kind', 'status', 'thing.name'])

        then:
        result == [num: '1', kind: 'VENDOR', status:[id:2, name:'Inactive'], thing: [name: 'Thing1']]
    }

    void "works with space in field and its null"() {
        when: 'a field has spaces'
        def ks = KitchenSink.build(1)
        def result = entityMapService.createEntityMap(ks, ['num', '  simplePogo.foo'])

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
        exp == entityMapService.createEntityMap(et, path)

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
        def resultsList = entityMapService.createEntityMapList(enummyList, ['*'])

        then:
        resultsList.size() == 2
        resultsList[0] == [id: 1, testEnum: 'FOO', version:null, enumIdent: [id:2, name:'Num2']]
        resultsList[1] == [id: 2, testEnum: 'FOO', version:null, enumIdent: [id:2, name:'Num2']]

        when:
        resultsList = entityMapService.createEntityMapList(enummyList, ['testEnum', 'enumIdent'])

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
        exp == entityMapService.createEntityMap(ks, path)

        where:
        path                          | exp
        ['thing.*']                   | [thing: [id:1, version:0, name:'Thing1', country:'US']]
        ['ext.name']                  | [ext: [name: 'SinkExt1']]
        ['sinkLink.ext.name']         | [sinkLink: [ext: [name: 'SinkExt1']]]
        ['bazMap', 'stringList']      | [bazMap: [foo: 'bar'], stringList:['red', 'blue', 'green']]
    }

    void "non association list should be wrapped too"() {
        when:
        def ks = KitchenSink.build(1)
        def emap = entityMapService.createEntityMap(ks, ['id', 'items'])

        then:
        ks.items
        emap.items
        emap.items instanceof EntityMapList

        when:
        def items = emap.items as EntityMapList

        then:
        items[0].keySet() == ['id'] as Set
    }

}
