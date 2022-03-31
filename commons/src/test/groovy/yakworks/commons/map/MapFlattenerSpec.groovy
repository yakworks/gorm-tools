/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.map

import java.time.LocalDate
import java.time.LocalDateTime

import spock.lang.Specification
import yakworks.commons.lang.IsoDateUtil

class MapFlattenerSpec extends Specification {

    MapFlattener mapFlattener = new MapFlattener()

    void "test parse date"() {
        setup:
        Map testMap = [
            customer  : [
                id     : 1,
                name   : 'bill',
                blah   : null,
                localDate   : LocalDate.parse('2021-02-01'),
                localDateTime  : LocalDateTime.parse("2017-10-19T11:40:00"),
                date3  : "2000-03-30",
                notDate: "200a0-03-30"
            ],
            keyContact: [
                id: 1
            ]
        ]

        when:
        Map res = MapFlattener.of(testMap).convertObjectToString(true).flatten()

        then:
        res.'customer.id' == "1"
        res.'customer.name' == 'bill'
        res.containsKey("customer.blah")
        res['customer.blah'] == null
        res.'keyContact.id' == '1'
        res["customer.localDate"] == '2021-02-01'
        res["customer.localDateTime"] == '2017-10-19T11:40:00'
        res["customer.date3"] == '2000-03-30'
        res["customer.notDate"] == "200a0-03-30"

    }

    void "test flatten list of ints"() {
        setup:
        Map testMap = [
            tags: [1, 2]
        ]

        when:
        Map res = MapFlattener.flattenMap(testMap)

        then:
        res['tags'] == [1, 2]
        res['tags.0'] == '1'
        res['tags.1'] == '2'
    }


    void "test flatten with objects"() {
        setup:
        def obj1 = LocalDate.parse("2021-01-01")
        Map testMap = [
            foo:[
                obj1: obj1,
                obj2: obj1
            ]
        ]

        when:
        Map res = MapFlattener.flattenMap(testMap)

        then:
        res['foo.obj1'] == obj1
        res['foo.obj2'] == obj1
    }


    void "test flatten with list of maps"() {
        setup:
        Map testMap = [
            tags: [[id: 1], [id: 2]]
        ]

        when:
        Map res = mapFlattener.of(testMap).convertObjectToString(true).flatten()

        then:
        res['tags'] == [[id: 1], [id: 2]]
        res['tags.0.id'] == '1'
        res['tags.1.id'] == '2'
    }

    void "test flatten with null values"() {
        setup:
        Map testMap = [
            book: [author: [id: 'null', name: ' foo ', age: 'null']]
        ]

        when:
        Map res = mapFlattener.flatten(testMap)

        then:
        res['book.author.id'] == "null"
        res['book.author.name'] == 'foo'
        res['book.author.age'] == null
    }

    void "test numbers"() {
        setup:
        Map testMap = [
            customer  : [
                id     : 1,
                amount   : 100.00
            ]
        ]

        when:
        Map res = MapFlattener.of(testMap).convertObjectToString(false).flatten()

        then:
        res.'customer.id' instanceof Integer
        res.'customer.amount' instanceof BigDecimal

    }
}
