/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.json

import org.springframework.mock.web.MockHttpServletRequest

import spock.lang.Specification

class HttpJsonParserTraitSpec extends Specification implements HttpJsonParserTrait {

    String sampleJson = '''
    {
        "amount": 6.01,
        "name": "Logan",
        "nested": { "foo": "bar" },
        "brothers": ["Wy Guy", "Goobie"]
    }
    '''

    void "test parseJson"() {
        when: 'parseJson should succeed with Object/Map data'
        def request = createMockRequest(sampleJson)
        Map data = parseJson(request, Map)

        then:
        data.amount == 6.01
        //since its a lax parser then quotes wont be required and should still get parsed
        data.name == 'Logan'
    }

    void "can modify parseJson result"() {
        when:
        def request = createMockRequest(sampleJson)
        //Map data = new LinkedHashMap(parseJson(request))
        Map data = parseJson(request, Map)

        // assert data instanceof LazyValueMap
        assert data.name
        data.name = 'Papa'
        assert data.nested.foo
        data.nested.foo = 'buzz'

        then:
        data.name == 'Papa'
        data.nested.foo == 'buzz'

    }

    void "test parseJson with bad or no data"() {
        when: 'request body empty'
        def request = createMockRequest('')
        Map data = parseJson(request, Map)

        then: 'parseJson should return empty map when request is empty'
        data == [:]

        when: 'request body is a list'
        String listJson = """
            ["Wy Guy", "Goobie"]
        """
        def requestWithListData = createMockRequest(listJson)
        Map listData = parseJson(requestWithListData, Map)

        then: 'parseJson should throw IllegalArgument'
        def e = thrown(IllegalArgumentException)
        e.message.contains('Json parsing expected a Map')
    }

    void "test parseJsonList"() {
        when: 'parseJson should succeed with Array/List data'
        String json = """
            ["Wy Guy", "Goobie"]
        """
        def request = createMockRequest(json)
        List data = parseJson(request, List)

        then:
        data.size() == 2
        data[0] == "Wy Guy"

    }

    void "test parseJsonList with bad data or empty request"() {
        when: 'request body empty'

        def request = createMockRequest('')
        List data = parseJson(request, List)

        then: 'parseJsonList should return empty List when request is empty'
        data == []

        when: 'request body is a map object'
        String listJson = """ { "foo": "bar" } """
        def requestWithMapData = createMockRequest(listJson)
        List listData = parseJson(requestWithMapData, List)

        then: 'parseJson should return empty list when data is not a map'
        def e = thrown(IllegalArgumentException)
        e.message.contains('Json parsing expected a List')
    }

    MockHttpServletRequest createMockRequest(String content){
        MockHttpServletRequest request = new MockHttpServletRequest()
        request.setContentType('application/json; charset=UTF-8')
        request.setContent(content.getBytes("UTF-8"))
        return request
    }

}
