package gorm.tools.testing

import grails.buildtestdata.BuildDataTest
import grails.testing.spring.AutowiredTest
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@SuppressWarnings(['JUnitPublicNonTestMethod', 'NoDef', 'FieldName', 'UnnecessarySelfAssignment'])
@CompileStatic
trait GormToolsDataTester implements JsonifyUnitTest, GormToolsTestHelper, BuildDataTest, AutowiredTest{

    void mockDomains(Class<?>... domainClassesToMock) {
        BuildDataTest.super.mockDomains(domainClassesToMock)
        mockRepositories(domainClassesToMock)
    }

    @CompileDynamic
    def <T> T buildCreate(Map args = [:], Class<T> clazz, Map renderArgs = [:]) {
        Map p = buildJson(args, clazz, renderArgs).json as Map
        clazz.create(p)
    }

}
