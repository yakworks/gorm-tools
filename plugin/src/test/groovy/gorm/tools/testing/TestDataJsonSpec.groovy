package gorm.tools.testing

import gorm.tools.testing.unit.DataRepoTest
import grails.buildtestdata.BuildDataTest
import spock.lang.Specification
import testing.Org
import testing.OrgExt

class TestDataJsonSpec extends Specification implements BuildDataTest, DataRepoTest{

    def setupSpec(){
        mockDomains(Org, OrgExt)
    }

    void "sanity check TestData.build"(){
        when:
        def build = build(Org, name: 'foo')//, includes: '*')

        then:
//        [secret:secret, name2:name2, amount:0, ext:testing.OrgExt : 1, inactive:false,
//         date:Thu Jan 25 19:36:02 CST 2018, locDateTime:2018-01-01T01:01:01, amount2:0, locDate:2018-01-25, extId:1, typeId:1, name:foo]
        TestTools.mapContains(build.properties, [id:1, name: 'foo'])
    }

    void "test getJsonIncludes"(){
        when:
        List incs = TestDataJson.getFieldsToBuild(Org)

        then:
        incs == ['name', 'type', 'type.id']

        when:
        incs = TestDataJson.getFieldsToBuild(Org, '*')

        then:
        incs == ['name', 'type', 'name2', 'secret', 'inactive', 'amount', 'amount2',
                 'date', 'locDate', 'locDateTime', 'ext', 'type.id']
    }

    void "test buildJson"(){
        expect:
        def buildJson = TestDataJson.buildJson(Org, name: 'foo')

        buildJson.jsonText == '{"name":"foo","type":{"id":1}}'
    }

//    void "test includes *"(){
//        when:
//        def buildJson = TestDataJson.buildJson(Org, name: 'bar', includes:'*')
//        then:
//        buildJson.json == '{"id":2,"isActive":false,"name":"bar"}'
//    }

}

