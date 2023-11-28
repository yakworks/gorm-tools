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

/**
 * Dirty status is stored in $changedProperties which is null initially
 instance.hasChanged() returns true if $changedProperties is null (thts strange)
 New instance is always dirty untill saved : That is because it has $changedProperties = null
 New instance is not dirty after save, even without flush :
 That is because ClosureEventTriggeringInterceptor.activateDirtyChecking sets $changedProperties to empty list if its null.
 ClosureEventTriggeringInterceptor gets called during hibernate's saveOrUpdate event` that even fires even without flush
 Existing instance is dirty untill saved with flush
 In this case dirty status is reset by GrailsEntityDirtinessStrategy.resetDirty which implements a CustomEntityDirtinessStrategy
 Dirty status does not get reset during saveOrUpdate event like in the case of new, because, ClosureEventTriggeringInterceptor does that only if changedProperties is null. and its not null in this case.
 */
class HasChangedSpec extends Specification implements GormHibernateTest {

    static entityClasses = [KitchenSink, SinkExt, SinkItem]

    void "hasChanged on new"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name")

        then: "new objects show up as changed"
        sink.hasChanged()
        sink.hasChanged("num")

        and: "but show even if it didnt actually get populated, so its not accurate on fields"
        sink.hasChanged("name2")
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

    void "PROBLEM - remains dirty untill flush"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        then:
        !sink.hasChanged()

        when:
        sink.name = "name2"

        then:
        sink.hasChanged()

        when: "persist should reset dirty status"
        sink.persist()

        then: "not dirty after save/persist"
        //XXX remains dirty untill we flush. thats same behavior as hibernate's isDirty
        sink.hasChanged('name') //not what we want
        sink.hasChanged() //not what we want
    }

    void "hasChanged change persist called twice or hasChanged called twice fails"() {
        when:
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()

        //this will trigger auditstamp in repo and update the edited time.
        sink.persist(flush:true)

        then:
        //this is fine
        !sink.hasChanged("num")
        //this is not
        !sink.hasChanged()

        when:
        sink.num = "456"

        then:
        //why does this fail now?
        sink.hasChanged("num")
        sink.hasChanged()

    }

    void "use HasChangedTesting to double persist"() {
        when:
        KitchenSink sink = HasChangedTesting.saveSink()
        sink.persist(flush:true)

        then:
        //this is fine
        !sink.hasChanged("num")
        //this is not
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

        when: "changes made to ext assoc"
        sink.persist(flush: true) //see comment below
        assert !sink.hasChanged()
        assert !sink.ext.hasChanged()
        sink.ext.name = "foo2"

        then: "ext shows changes"
        sink.ext.hasChanged()
        sink.ext.hasChanged("name")

        and: "the main sink does not show so it does not cascadem which is obvious when looking at source"
        !sink.hasChanged()
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

        when: "changes made to thing assoc"
        sink.persist(flush: true) //see comment below
        assert !sink.hasChanged()
        sink.thing.name = "thing2"

        then:
        sink.thing.hasChanged()
        sink.thing.hasChanged("name")
        //sink does not show changes unlike with belongsTo
        !sink.hasChanged()
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
        //XXX same as above, only works with flush
        // sink.persist()
        //it seems to work with flush but why, thats not consistent?
        sink.persist(flush: true)

        then:
        //HERE BE DRAGONS, why is this failing without flush:true? its like the second persist doesnt clear? or is it just spock?
        // maybe put in normal class and see if its does the same thing or must if be flushed to be accurate? which is kind of bad.
        !sink.hasChanged()
        !sink.hasChanged("num")

        when:
        sink.num = "456"

        then:
        !sink.hasChanged()
        !sink.hasChanged("num")

    }
}
