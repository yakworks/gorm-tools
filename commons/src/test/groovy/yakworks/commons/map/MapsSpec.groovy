/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.map

import spock.lang.Specification

class MapsSpec extends Specification {

    def "merge a single map should return it self"(){

        when:
        def m0 = [
            foo: 'bar'
        ]

        then:"The merge is correct"
        assertMapsEqual(m0, Maps.merge(m0))
    }

    void testMerge_Two_NoNesting_NoOverwriting () {
        when:
        def m0 = [
            foo: 'bar'
        ]

        def m1 = [
            baz: 'qux'
        ]

        def expected = [
            foo: 'bar',
            baz: 'qux'
        ]
        def merged = Maps.merge(m0, m1)

        then:
        expected == merged
        // assertMapsEqual(expected, instance.merge(m0, m1))
    }

    void "merge with list" () {
        when:
        def m0 = [
            foo: 'bar'
        ]

        def m1 = [
            foo: 'buzz',
            baz: 'qux'
        ]

        def expected = [
            foo: 'buzz',
            baz: 'qux'
        ]

        def listOfMaps = [m0, m1]
        def mergedMap = Maps.merge(listOfMaps)

        then:
        m0.size() == 1
        m1.size() == 2
        mergedMap.size() == 2
        assertMapsEqual(expected, mergedMap)
    }

    void testMerge_Two_NoNesting_WithOverwriting () {
        when:
        def m0 = [
            foo: 'bar'
        ]

        def m1 = [
            foo: 'baz'
        ]

        def expected = [
            foo: 'baz'
        ]

        then:
        assertMapsEqual(expected, Maps.merge(m0, m1))
    }


    void testMerge_Three_NoNesting_WithOverwriting () {
        when:
        def m0 = [
            foo: 'bar'
        ]

        def m1 = [
            foo: 'baz'
        ]

        def m2 = [
            foo: 'qux'
        ]

        def expected = [
            foo: 'qux'
        ]
        then:
        assertMapsEqual(expected, Maps.merge(m0, m1, m2))
    }

    void testMerge_Two_NestedOneLevel_NoOverwriting () {
        when:
        def m0 = [
            foo: [
                bar: 'baz'
            ]
        ]

        def m1 = [
            qux: [
                bar: 'baz'
            ]
        ]

        def expected = [
            foo: [
                bar: 'baz'
            ],
            qux: [
                bar: 'baz'
            ]
        ]
        then:
        assertMapsEqual(expected, Maps.merge(m0, m1))
    }


    void testMerge_Two_NestedOneLevel_OverwriteNonMaps () {
        when:
        def m0 = [
            foo: 'bar',
            baz: [
                qux: 'corge'
            ]
        ]

        def m1 = [
            foo: 'waldo',
            corge: [
                grault: 'garply'
            ]
        ]

        def expected = [
            foo: 'waldo',
            baz: [
                qux: 'corge'
            ],
            corge: [
                grault: 'garply'
            ]
        ]
        then:
        assertMapsEqual(expected, Maps.merge(m0, m1))
    }


    void testMerge_Three_NestedTwoLevels_RatherComplex () {
        when:
        def m0 = [
            foo: 'bar',
            baz: [
                qux: 'corge',
                fred: [
                    plugh: 'waldo'
                ]
            ]
        ]

        def m1 = [
            foo: 'thud',
            baz: [
                quux: 'corge',
                fred: [
                    waldo: 'plugh',
                    spam: 'ham',
                    eggs: 'bacon'
                ],
                walrus: [
                    otter: 'hamster'
                ]
            ]
        ]


        def m2 = [
            baz: [
                fred: [
                    hippo: 'rhino'
                ]
            ],
            quiver: 'shatter'
        ]

        def expected = [
            baz: [
                fred: [
                    eggs: 'bacon',
                    hippo: 'rhino',
                    plugh: 'waldo',
                    spam: 'ham',
                    waldo: 'plugh'
                ],
                quux: 'corge',
                qux: 'corge',
                walrus: [
                    otter: 'hamster'
                ]
            ],
            foo: 'thud',
            quiver: 'shatter'
        ]
        then:
        assertMapsEqual(expected, Maps.merge(m0, m1, m2))
    }


    void testMerge_Two_NoNesting_OverwritesList () {
        when:
        def m0 = [
            foo: [
                'bar',
                'baz'
            ]
        ]

        def m1 = [
            foo: [
                'qux',
                'quux'
            ]
        ]

        def expected = [
            foo: [
                'bar',
                'baz',
                'qux',
                'quux'
            ]
        ]
        then:
        assertMapsEqual(expected, Maps.merge(m0, m1))
    }

    void deepPrune () {
        when:
        def mp = [
            foo: 'thud',
            fuzz: '',
            fazz: null,
            fozz: [:],
            fizz: [],
            isFoo: false ,
            isFuzz: 1,
            baz: [
                quux: 'corge',
                fred: [
                    waldo: null
                ]
            ]
        ]

        def m1 = Maps.prune(mp, false)

        def expected = [
            foo: 'thud',
            fuzz: '',
            fozz: [:],
            fizz: [],
            isFoo: false,
            isFuzz: 1,
            baz: [
                quux: 'corge',
                fred: [:]
            ]
        ]

        then:
        expected == m1
        assertMapsEqual(expected, m1)

        when:
        def m2 = Maps.prune(mp)

        expected = [
            foo: 'thud',
            isFoo: false,
            isFuzz: 1,
            baz: [
                quux: 'corge'
            ]
        ]
        then: "with pruneEmpty true"

        expected == m2
        assertMapsEqual(expected, m2)
    }

    void "test removePropertyListKeys"(){
        when:
        def mp = [
            foobar: 'thud',
            foo: [1, 12],
            'foo[0]': 0,
            'foo[1]': 12,
            baz: [
                quux: 'corge',
                'foo[0]': 0,
                'foo[1]': 12,
            ]
        ]

        def m1 = Maps.removePropertyListKeys(mp)

        def expected = [
            foobar: 'thud',
            foo: [1, 12],
            baz: [
                quux: 'corge',
                foo: [0, 12]
            ]
        ]

        then:
        expected == m1
        assertMapsEqual(expected, m1)
    }

    void "test clone is deep"() {
        given:
        Map source = [num1:1, num2:2, nested:[num1:1, num2:2], list:[1,2,3], listOfMap:[[one:1]]]

        when:
        Map copy = Maps.clone(source)
        //change source to make sure we are dealing with copy
        source.nested.num1 = 99
        source.list.add(9)
        source.listOfMap[0]['one'] = 99

        then:
        !copy.is(source)
        !copy.nested.is(source.nested)
        !copy.list.is(source.list)
        !copy.listOfMap[0].is(source.listOfMap[0]) //maps inside list should not have been copied by reference
        copy.listOfMap[0].one == 1
        assertMapsEqual(copy, [num1:1, num2:2, nested:[num1:1, num2:2], list:[1,2,3], listOfMap:[[one:1]]])
    }

    void "test clone list of maps"() {
        given:
        List source = [[num1:1, num2:2, nested:[num1:1, num2:2], list:[1,2,3], listOfMap:[[one:1]]]]

        when:
        Collection<Map> copy = Maps.clone(source)
        //change source to make sure we are dealing with copy
        source[0].nested.num1 = 99
        source[0].list.add(9)
        source[0].listOfMap[0]['one'] = 99

        then:
        !copy.is(source)
        !copy[0].nested.is(source[0].nested)
        !copy[0].list.is(source[0].list)
        !copy[0].listOfMap[0].is(source[0].listOfMap[0]) //maps inside list should not have been copied by reference
        copy[0].listOfMap[0].one == 1
        assertMapsEqual(copy[0], [num1:1, num2:2, nested:[num1:1, num2:2], list:[1,2,3], listOfMap:[[one:1]]])
    }

    void "test getBoolean"() {
        when:
        def maps = [foo: true, bar: false]


        then:
        Maps.getBoolean('foo', maps)
        !Maps.getBoolean('bar', maps)
        Maps.getBoolean('nothing', maps, true)
        !Maps.getBoolean('nothing2', maps, false)
    }

    void "test boolean"() {
        when:
        def maps = [foo: true, bar: false]


        then:
        Maps.boolean(maps, 'foo')
        !Maps.boolean(maps, 'bar')
        Maps.boolean(maps, 'nothing', true)
        !Maps.boolean(maps, 'nothing2', false)
    }

    void "test deep merge"() {
        given:
        Map m1 = [num1:1, num2:2, nested:[num1:1, num2:2], list:[1,2,3]]
        Map m2 = [num1:9, num3:3, nested:[num1:9, num3:3], list:[4]]

        when:
        Map copy = Maps.merge(m1, m2)
        //modify to make sure have copy
        m1.nested.num1 = 99
        m2.nested.num1 = 99
        m1.list << 99
        m2.list << 99

        then:
        assertMapsEqual(copy, [num1:9, num2:2, num3:3, nested:[num1:9, num2:2, num3:3], list:[1,2,3,4]])
    }

    // A couple rather crude map equality testers.
    private void assertMapsEqual(expected, actual) {
        compareMapsWithAssertions(expected, actual)
        compareMapsWithAssertions(actual, expected)
    }

    private void compareMapsWithAssertions(expected, actual) {
        expected.each { k, v ->
            if (v instanceof Map) {
                compareMapsWithAssertions(expected[k], actual[k])
            } else {
                assert v == actual[k]
            }
        }
    }
}
