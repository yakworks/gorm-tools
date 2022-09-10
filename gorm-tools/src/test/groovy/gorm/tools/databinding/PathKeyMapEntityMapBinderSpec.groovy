/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.databinding


import yakworks.gorm.testing.unit.DataRepoTest
import spock.lang.Specification
import yakworks.gorm.testing.model.KitchenSink

class PathKeyMapEntityMapBinderSpec extends Specification implements DataRepoTest {
    EntityMapBinder binder

    void setup() {
        binder = new EntityMapBinder()
    }

    Class[] getDomainClassesToMock() {
        [KitchenSink]
    }

    void "test bind using PathKeyMap"() {
        Map sub = [
            name2: "name2",
            inactive: "true",
            amount: "100.00",
            "sinkLink.name2": "sinkLink.name2",
            "thing.name" : "thing"
        ]

        when:
        PathKeyMap params = PathKeyMap.of(sub).init()
        KitchenSink sink = new KitchenSink()
        binder.bind(sink, params)

        then:
        sink.name2 == "name2"
        sink.inactive == true
        sink.amount == 100.00
        sink.sinkLink.name2 == "sinkLink.name2"
        sink.thing == null //this is not bindable
    }


}
