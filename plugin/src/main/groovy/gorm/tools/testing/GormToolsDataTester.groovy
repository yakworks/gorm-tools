package gorm.tools.testing

import gorm.tools.json.Jsonify
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.TestData
import grails.views.json.test.JsonViewUnitTest

@SuppressWarnings(['JUnitPublicNonTestMethod', 'NoDef', 'FieldName', 'UnnecessarySelfAssignment'])
trait GormToolsDataTester implements JsonifyUnitTest, GormToolsTestHelper, BuildDataTest {

    void mockDomains(Class<?>... domainClassesToMock) {
        BuildDataTest.super.mockDomains(domainClassesToMock)
        mockRepositories(domainClassesToMock)
    }

    Jsonify.JsonifyResult buildJson(Map args = [:], Class clazz, Map renderArgs = [:]) {
        Object obj = TestData.build(args, clazz)
        return Jsonify.render(obj, renderArgs)
    }

}
