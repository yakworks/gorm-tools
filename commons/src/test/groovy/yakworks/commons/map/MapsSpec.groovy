/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.map

import spock.lang.Specification

class MapsSpec extends Specification {
    def instance = new Maps()

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
        then:
        assertMapsEqual(expected, instance.merge(m0, m1))
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
        assertMapsEqual(expected, instance.merge(m0, m1))
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
        assertMapsEqual(expected, instance.merge(m0, m1, m2))
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
        assertMapsEqual(expected, instance.merge(m0, m1))
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
        assertMapsEqual(expected, instance.merge(m0, m1))
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
        assertMapsEqual(expected, instance.merge(m0, m1, m2))
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
                'qux',
                'quux'
            ]
        ]
        then:
        assertMapsEqual(expected, instance.merge(m0, m1))
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
