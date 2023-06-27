/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.model.Thing
import yakworks.testing.gorm.unit.GormHibernateTest

class HasChangedSpec extends Specification implements GormHibernateTest {

    static entityClasses = [KitchenSink, SinkExt, SinkItem]

    void "hasChanged on new"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name")

        then:
        sink.hasChanged()
        sink.hasChanged("num")
    }

    void "hasChanged change persisted"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()

        when:
        //change trait field
        sink.num = "456"
        //change KitchenSink field
        sink.name2 = "foo"

        then:
        sink.hasChanged("num")
        sink.hasChanged("name2")
        sink.hasChanged()

    }

    void "markDirty works"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()
        sink.markDirty()

        then:
        sink.hasChanged()

        when:
        sink.num = "456"

        then:
        sink.hasChanged("num")
        sink.hasChanged()

    }

    void "hasChanged change persist called twice or hasChanged called twice fails"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()
        //why does this make it fail?
        sink.persist()

        then:
        !sink.hasChanged()

        when:
        sink.num = "456"

        then:
        //why does this fail now?
        sink.hasChanged("num")
        sink.hasChanged()

    }

    void "hasChanged with association with belongsto"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()
        !sink.hasChanged("ext")

        when:
        sink.ext = new SinkExt(kitchenSink: sink, name: "foo", textMax: 'fo')

        then:
        sink.hasChanged()
        sink.hasChanged("ext")
    }

    void "hasChanged with free association"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()
        !sink.hasChanged("thing")

        when:
        sink.thing = new Thing(name: "thing1")

        then:
        sink.hasChanged()
        sink.hasChanged("thing")
        sink.thing.hasChanged()
    }

    void "hasChanged with saved association"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()
        !sink.hasChanged("thing")

        when:
        sink.thing = new Thing(name: "thing1").persist()

        then:
        sink.hasChanged()
        sink.hasChanged("thing")
        !sink.thing.hasChanged()
    }

    void "hasChanged with hasMany"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()
        !sink.hasChanged("stringList")

        when:
        sink.stringList = ['foo', 'bar']

        then:
        sink.hasChanged()
        sink.hasChanged("stringList")
    }

    void "hasChanged when same value is set"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()

        when:
        sink.num = "123"

        then:
        !sink.hasChanged()
        !sink.hasChanged("num")

        when:
        sink.num = "456"

        then:
        sink.hasChanged()
        sink.hasChanged("num")

        when:
        sink.persist()
        //it seems to work with flush but thats not consistent?
        //sink.persist(flush: true)

        then:
        //HERE BE DRAGONS, why is this failing? its like the second persist doesnt clear? or is it just spock?
        // maybe put in normal class and see what it does
        !sink.hasChanged()
        !sink.hasChanged("num")

        when:
        sink.num = "456"

        then:
        !sink.hasChanged()
        !sink.hasChanged("num")

    }
}
