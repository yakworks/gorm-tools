/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

@Ignore //XXX shows the problems with isDirty
class IsDirtySpec extends Specification implements GormHibernateTest {

    static entityClasses = [KitchenSink, SinkExt, SinkItem]

    void "isDirty on new does not work"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name")

        then:
        //this fails when new
        sink.isDirty()
        //this fails too when new
        sink.isDirty("num")

    }

    void "isDirty change persisted"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.isDirty()

        when:
        //change trait field
        sink.num = "456"
        //change KitchenSink field
        sink.name2 = "foo"

        then:
        //these now work?
        sink.isDirty("num")
        sink.isDirty("name2")
        sink.isDirty()

    }

    void "markDirty does not work with isdirty"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()
        sink.markDirty()

        then:
        //this fails
        sink.isDirty()

        when:
        sink.num = "456"

        then:
        //these now work?
        sink.isDirty("num")
        sink.isDirty()
    }

    void "isDirty change persist called twice or isdirty called twice fails"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()
        sink.persist()
        then:
        !sink.isDirty()

        when:
        sink.num = "456"

        then:
        //why does this fail now?
        sink.isDirty("num")
        sink.isDirty()

    }

    void "isDirty with association"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.isDirty()
        !sink.isDirty("num")

        when:
        sink.ext = new SinkExt(kitchenSink: sink, name: "foo", textMax: 'fo')

        then:
        //blows TransientObjectException, why?
        sink.isDirty()
        sink.isDirty("ext")
    }

}
