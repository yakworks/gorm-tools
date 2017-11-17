package gorm.tools.mango

import spock.lang.Specification

class MangoTidyMapSpec extends Specification {

    void "test in"() {
        when:
        def mmap = tidy([
            'foo.id':[1,2,3],
            'customer.id': ['$in': [1,2,3]]
        ])

        then:
        mmap == [
            foo: [
                id: [
                    '$in': [1,2,3]
                ]
            ],
            customer:[
                id: [
                    '$in': [1,2,3]
                ]
            ]
        ]

        when:
        mmap = tidy([
            "customer": [["id":1],["id":2],["id":3]]
        ])

        then:
        mmap == [
            customer: [
                id: [
                    '$in': [1,2,3]
                ]
            ]
        ]
    }

    Map tidy(Map m){
        MangoTidyMapSpec.tidy(m)
    }
}
