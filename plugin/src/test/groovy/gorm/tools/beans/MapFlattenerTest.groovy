package gorm.tools.beans

import spock.lang.Specification

class MapFlattenerTest extends Specification {

    MapFlattener mapFlattener = new MapFlattener()

    void "test parse date"() {
        setup:
        Map testMap = [
            customer  : [
                id     : 1,
                name   : 'bill',
                blah   : null,
                date   : "2000-03-30T22:00:00Z",
                date2  : "2000-03-30T22:00:00.000Z",
                date3  : "2000-03-30",
                notDate: "200a0-03-30"
            ],
            keyContact: [
                id: 1
            ]
        ]

        when:
        Map res = mapFlattener.flatten(testMap)

        then:
        res.'customer.id' == "1"
        res.'customer.name' == 'bill'
        res.containsKey("customer.blah")
        res['customer.blah'] == null
        res.'keyContact.id' == '1'
        res["customer.date"] == IsoDateUtil.format(IsoDateUtil.parse(testMap.customer.date))
        res["customer.date2"] == IsoDateUtil.format(IsoDateUtil.parse(testMap.customer.date2))
        res["customer.date3"] == IsoDateUtil.format(IsoDateUtil.parse(testMap.customer.date3))
        res["customer.notDate"] == "200a0-03-30"

    }

    void "test flatten list of ints"() {
        setup:
        Map testMap = [
            tags: [1, 2]
        ]

        when:
        Map res = mapFlattener.flatten(testMap)

        then:
        res['tags'] == [1, 2]
        res['tags.0'] == '1'
        res['tags.1'] == '2'
    }


    void "test flatten with list of maps"() {
        setup:
        Map testMap = [
            tags: [[id: 1], [id: 2]]
        ]

        when:
        Map res = mapFlattener.flatten(testMap)

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
}
