/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import spock.lang.Specification
import testing.Address
import yakworks.testing.gorm.model.KitchenSink

import static gorm.tools.mango.MangoTidyMap.tidy


class MangoTidyMapSpec extends Specification {

    void "test pathToMap"() {
        expect:
        [a: [b: [c: 1]]] == MangoTidyMap.pathToMap("a.b.c", 1, [:])
        flatten([a: [b: [c: 1]], d: 2]) == flatten(MangoTidyMap.pathToMap("a.b.c", 1, [d: 2]))
        flatten([a: 1, d: 2]) == flatten(MangoTidyMap.pathToMap("a", 1, [d: 2]))
    }

    void "test tidy method for equal"() {
        expect:
        [a: [b: [c: ['$eq': 1]]]] == tidy(["a.b.c": 1])
        [a: [b: [c: ['$eq': 1]]], d: ['$eq': 2]] == tidy(["a.b.c": 1, d: ['$eq': 2]])
        [a: ['$eq': 1], d: ['$eq': 2]] == tidy(["a": 1, d: 2])
        [a: [b: [c: ['$eq': 1]]]] == tidy([a: [b: [c: 1]]])
    }

    void "test id not broken out"() {

        when: 'deep nesting'
        def mmap = tidy([
            'foo.bar.baz.id'  : 1,
            'foo.bar.baz.sid' : 2,
            'foo.bar.baz.sad' : 3

        ])

        then: 'doesnt do it with id?'
        mmap == [
            foo:[
                bar:[
                    baz:[
                        id:[$eq:1],
                        sid:[$eq:2],
                        sad:[$eq:3]
                    ]
                ]
            ]
        ]
    }


    void "test in"() {

        when:
        def mmap = tidy([
            'foo.id'     : [1, 2, 3],
            'customer.id': ['$in': [1, 2, 3]]
        ])

        then:
        mmap == [
            foo: [
                id: ['$in': [1, 2, 3] ]
            ],
            customer: [
                id: ['$in': [1, 2, 3]]
            ]
        ]

        when:
        mmap = tidy([
            "customer": [["id": 1], ["id": 2], ["id": 3]]
        ])

        then:
        mmap == [
            customer: [
                id: [
                    '$in': [1, 2, 3]
                ]
            ]
        ]

        when: "id is not 1st key"
        mmap = tidy([
            "customer": [[name:"x1", "id": 1], [name:"x2", "id": 2], ["id": 3, name:"x3"]]
        ])

        then:
        mmap == [
            customer: [
                id: [
                    '$in': [1, 2, 3]
                ]
            ]
        ]

        when:
        mmap = tidy([
            "num": ['num1', 'num2']
        ])

        then:
        mmap == [
            num: [
                    '$in': ['num1', 'num2']
            ]
        ]
    }

    void "test like"() {
        when:
        def mmap = tidy([
            'foo.name': "Name%"
        ])

        then:
        mmap == [foo: [name: ['$ilike': "Name%"]]]

        when:
        mmap = tidy(
            [foo:
                 [name: "Name%"]
            ])

        then:
        mmap == [foo: [name: ['$ilike': "Name%"]]]

        when: "wrapped in a not"
        mmap = tidy([
            '$not':[
                'foo':[
                    name: "Name%"
                ]
            ]
        ])

        then: "converts it to a list"
        mmap == [
            '$not': [ //list
                [foo: [name: ['$ilike': "Name%"]]]
            ]
        ]

        when: "wrapped in a not shortcut"
        mmap = tidy([
            '$not':[
                'foo.name': "Name%",
                'buzz.boo': "bar%"
            ]
        ])

        then: "converts it to a list of maps"
        mmap == [
            '$not': [
                [foo: [name: ['$ilike': "Name%"]]],
                [buzz: [boo: ['$ilike': 'bar%']]]
            ]
        ]
    }

    void "test like with star *"() {
        when: 'its already in $ilike'
        def mmap = tidy([
            name: ['$ilike': "Name*"]
        ])

        then: 'it gets replaced with %'
        mmap == [name: ['$ilike': "Name%"]]

        when: 'its already in $like'
        mmap = tidy([
            name: ['$like': "*ame*"]
        ])

        then: 'it gets replaced with %'
        mmap == [name: ['$like': "%ame%"]]

        when:
        mmap = tidy([
            'foo.name': "Name*"
        ])

        then:
        mmap == [foo: [name: ['$ilike': "Name%"]]]

        when:
        mmap = tidy(
            [foo:
                 [name: "Name*"]
            ])

        then:
        mmap == [foo: [name: ['$ilike': "Name%"]]]

        when: "wrapped in a not"
        mmap = tidy([
            '$not':[
                'foo':[
                    name: "Name*"
                ]
            ]
        ])

        then: "converts it to a list"
        mmap == [
            '$not': [ //list
                      [foo: [name: ['$ilike': "Name%"]]]
            ]
        ]

        when: "wrapped in a not shortcut"
        mmap = tidy([
            '$not':[
                'foo.name': "Name*",
                'buzz.boo': "bar*"
            ]
        ])

        then: "converts it to a list of maps"
        mmap == [
            '$not': [
                [foo: [name: ['$ilike': "Name%"]]],
                [buzz: [boo: ['$ilike': 'bar%']]]
            ]
        ]
    }


    void "test not simple"() {
        when: 'the $not is a simple map'
        def mmap = tidy([
            '$not': [
                id: 123
            ]
        ])

        then: 'has single item in the list'
        mmap == [
            '$not': [
                [ id: ['$eq':123] ]
            ]
        ]
    }

    void "test not multiple keys in map"() {
        when: 'the $not is map with multiple keys'
        def mmap = tidy([
            '$not': [
                id: 123, name: 'bill'
            ]
        ])

        then: 'has 2 items in list'
        mmap == [
            '$not': [
                [   id: ['$eq': 123] ],
                [ name: ['$eq': 'bill'] ]
            ]
        ]
    }

    void "test not with list"() {
        when: 'the $not is map with multiple keys'
        def mmap = tidy([
            '$not': [
                [id: ['$eq': 1]],
                [id: ['$eq': 2]]
            ]
        ])

        then: 'has same items in list'
        mmap == [
            '$not': [
                [id: ['$eq': 1]],
                [id: ['$eq': 2]]
            ]
        ]
    }

    void "test not tags"() {
        when: 'the $not is a map'
        def mmap = tidy([
            '$not':[
                tags:[
                    [id:9], [id:10]
                ]
            ]
        ])

        then: 'has single item in the list'
        mmap == [
            '$not':[
                [tags:[
                    id:[ $in:[9, 10] ]
                ]]
            ]
        ]

        when: 'the $not is a single item in list'
        mmap = tidy([
            '$not':[
                [ id: [9,10]]
            ]
        ])

        then: 'has just the single item'
        mmap == [
            '$not':[
                [
                    id:[ $in:[9, 10] ]
                ]
            ]
        ]

        when: 'the $not is a list'
        mmap = tidy([
            '$not':[
                [ tags:[ [id:9], [id:10] ] ],
                //[ id: 123 ]
            ]
        ])

        then: 'has single item in the list'
        mmap == [
            '$not':[
                [
                    tags:[
                        id:[ $in:[9, 10] ]
                    ]
                ],
                // [
                //     id: ['$eq': 123]
                // ]
            ]
        ]

    }

    void "test eq"() {

        when: "object is assigned"
        def loc = new Address()
        def mmap = tidy('location': loc)

        then:
        mmap == [location: ['$eq': loc]]

        when:
        mmap = tidy([
            'foo.name': "Name"
        ])

        then:

        mmap == [foo: [name: ['$eq': "Name"]]]

        when:
        mmap = tidy([
            foo: [name: "Name"]
        ])

        then:

        mmap == [foo: [name: ['$eq': "Name"]]]
    }

    void "test combined methods"() {
        when:
        def mmap = tidy([
            "customer": [
                "id"  : 101,
                "name": "Wal%"
            ]
        ])

        then:
        mmap == [customer: [id: ['$eq': 101], name: ['$ilike': 'Wal%']]]

    }

    void "test \$or"() {
        when:
        def mmap = tidy([
            '$or': [
                "id"  : 101,
                "name": "Wal%"
            ]
        ])

        then:

        mmap == ['$or': [
            [id: ['$eq': 101]],
            [name: ['$ilike': 'Wal%']]
        ]]

    }

    void "test deep \$or"() {
        when:
        def mmap = tidy([
            '$or': [
                ['ext.thing.name': 'Thing1'],
                ['ext.thing.id': 123]
            ]
        ])

        then:

        mmap == ['$or': [
            [ext:[thing:[name:[$eq:'Thing1']]]],
            [ext:[thing:[id:[$eq:123]]]]
        ]]

    }


    void "test \$and"() {
        when:
        def mmap = tidy([
            '$and': [
                "id"  : 101,
                "name": "Wal%"
            ]
        ])

        then:

        mmap == ['$and': [
            [id: ['$eq': 101]],
            [name: ['$ilike': 'Wal%']]
        ]]

    }

    void "test \$or with implied \$and "() {
        when:
        def mmap = tidy([
            '$or': [
                ["location.id": 5],
                ["name": "Name1", "num": "num1"] //implied nested $and
            ]
        ])

        then:

        mmap == [
            '$or':[
                [
                    'location':[
                        id: [$eq:5]
                    ]
                ],
                [
                    $and:[
                        [name:[$eq: 'Name1']],
                        [num:[$eq: 'num1']]
                    ]
                ]
            ]
        ]

    }

    void "test % converted to ilike"() {
        when: 'NOT using $eq specifically it will convert to $ilike with %'
        def mmap = tidy([
            'foo.name': "Name%"
        ])

        then:
        mmap == [foo: [name: ['$ilike': "Name%"]]]

        when: 'using $eq specifically it will NOT convert to $ilike with %'
        mmap = tidy([
            'foo.name.$eq': "Name%"
        ])

        then:
        mmap == [foo: [name: ['$eq': "Name%"]]]

    }

    void "test \$or with and"() {
        when:
        def mmap = tidy(['$or': [["address.id": 5], ["name": "Org#1", "address.id": 4]]])

        then:

        // flatten(mmap) == flatten([
        //     '$or': [
        //         [
        //             [ 'address.id': ['$eq': 5] ]
        //         ],
        //         [
        //             '$and': [
        //                 [name: ['$eq': "Org#1"] ],
        //                 ['address.id': ['$eq': 4] ]
        //             ]
        //         ]
        //     ]
        // ])

        mmap == [
            $or: [
                [ address: [id: [$eq: 5] ] ],
                [
                    $and: [
                        [name: [$eq: "Org#1"] ],
                        [address: [id: [$eq: 4] ] ]
                    ]
                ]
            ]
        ]
    }


    void "test if map has Mango method "() {
        when:
        def mmap = tidy([
            'foo.name.$eq': "Name"
        ])

        then:
        mmap == [foo: [name: ['$eq': "Name"]]]

        when:
        mmap = tidy([
            'foo.name.$like': "Name*"
        ])

        then:
        mmap == [foo: [name: ['$like': "Name%"]]]

        when:
        mmap = tidy([
            foo: ['name.$like': "Name"]
        ])

        then:
        mmap == [foo: [name: ['$like': "Name"]]]

        when:
        mmap = tidy([
            'foo.name.$eq': "Name%"
        ])

        then:
        mmap == [foo: [name: ['$eq': "Name%"]]]

    }

    void "test sort"() {
        when:
        def mmap = tidy('$sort':['location.address': "desc"])

        then:
        mmap == ['$sort':['location.address': "desc"]]

        when:
        mmap = tidy('$sort':['foo.bar.baz': "asc"])

        then:
        mmap == ['$sort':['foo.bar.baz': "asc"]]

        when:
        mmap = tidy('$sort':'location.address')

        then:
        mmap == ['$sort':'location.address']

        // when:
        // mmap = tidy('$sort':'name desc')
        //
        // then:
        // mmap == ['$sort':[name: 'desc']]
        //
        // when:
        // mmap = tidy('$sort':'name asc, foo desc')
        //
        // then:
        // mmap == ['$sort':[name: 'asc', foo: 'desc']]
    }

    void "test exists"() {

        when:
        def crit = new MangoDetachedCriteria(KitchenSink)
        def mmap = tidy([
            '$exists': crit
        ])

        then:
        mmap == [ '$exists': crit]

        when:
        mmap = tidy([
            '$not': [ '$exists': crit]
        ])

        then: "converts not to a list of maps"
        mmap == [
            '$not': [
                [ '$exists': crit ]
            ]
        ]

        when:
        mmap = tidy([
            '$not':[
                '$exists': crit,
                'name': 'org4*'
            ]
        ])

        then: "converts not to a list of maps"
        mmap == [
            '$not': [
                [ '$exists': crit ],
                ['name': ['$ilike': "org4%"]]
            ]
        ]

    }


    Map flatten(Map m, String separator = '.') {
        m.collectEntries { k, v -> v instanceof Map ? flatten(v, separator).collectEntries { q, r -> [(k + separator + q): r] } : [(k): v] }
    }

}
