/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing

import gorm.tools.testing.unit.DataRepoTest
import grails.buildtestdata.BuildDataTest
import spock.lang.Specification
import testing.Cust

class TestDataJsonSpec extends Specification implements BuildDataTest, DataRepoTest{

    def setupSpec(){
        mockDomains(Cust)
    }

    void "sanity check TestData.build"(){
        when:
        def build = build(Cust, name: 'foo')//, includes: '*')

        then:
//        [secret:secret, name2:name2, amount:0, ext:testing.OrgExt : 1, inactive:false,
//         date:Thu Jan 25 19:36:02 CST 2018, locDateTime:2018-01-01T01:01:01, amount2:0, locDate:2018-01-25, extId:1, typeId:1, name:foo]
        TestTools.mapContains(build.properties, [id:1, name: 'foo'])
    }

    void "test getJsonIncludes"(){
        when:
        List incs = TestDataJson.getFieldsToBuild(Cust)

        then:
        incs == ['name', 'type', 'type.id']

    }

    void "test buildMap"(){
        expect:
        def map = TestDataJson.buildMap(Cust)

        map == [name: 'name', type:[id:1]]
    }

    void "test buildJson"(){
        expect:
        def buildJson = TestDataJson.buildJson(Cust, name: 'foo', name2: 'bar')

        buildJson.jsonText == '{"name":"foo","type":{"id":1},"name2":"bar"}'
    }

}
