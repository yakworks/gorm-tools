/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.testing.pogos

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileStatic

/**
 * A sample domain model for ktichen sink testing
 */
@CompileStatic
class Gadget {
    Long id
    long id2

    String name

    // booleans
    Boolean inactive = false
    boolean enabled

    // numbers
    BigDecimal bigDecimal

    // dates
    Date oldDate
    LocalDate localDate
    LocalDateTime localDateTime

    // objects
    Map mapData
    List stringList
    List<Thing> thingList
    Thing thing
    //self reference
    Gadget link

    //enums
    Kind kind // internal
    GadgetStatus status //id enum

    static enum Kind {CHILD, PARENT}

    //built in enums
    //Currency currency //FIXME creates and overflow
    //Country countryCode
    //TimeZone timeZone

    static Gadget create(Long id){
        return new Gadget(
            id: id,
            id2: id,
            name: "Kitchen${id}",
            inactive: false,
            enabled: true,
            bigDecimal: id + 99.99,
            oldDate: new Date(),
            localDate: LocalDate.now().plusDays(id),
            localDateTime: LocalDateTime.now().plusDays(id),
            kind: (id % 2) ? Gadget.Kind.CHILD : Gadget.Kind.PARENT ,
            status: (id % 2) ? GadgetStatus.Inactive : GadgetStatus.Active,
            //objects
            thing: new Thing(name: "thingy${id+100}"),
            thingList: [Thing.of(id+101, "Thingy${id+101}"), Thing.of(id+102, "Thingy${id+102}")],
            mapData: [foo: "foo1", bar: 'bar1'],
            stringList: ['rand', 'galt'],
            link: new Gadget(name: "linked gadget")
        )
    }

    static List<Gadget> buildGadgets(int count){
        List<Gadget> gadgets = []
        (1..count).each { id ->
            gadgets << Gadget.create(id)
        }
        return gadgets
    }
}
