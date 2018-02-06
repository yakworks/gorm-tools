package gorm.tools.testing

import grails.buildtestdata.BuildDataTest
import grails.testing.spring.AutowiredTest
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * Base test class to use as a drop in replacement of DataTest and GormToolsTest,
 * will set up the repositories properly for the mocked domains and adds all the methods from
 * build-test-data plugin's BuildDataTest.
 */
//@SuppressWarnings(['JUnitPublicNonTestMethod', 'NoDef', 'FieldName', 'UnnecessarySelfAssignment'])
@CompileStatic
trait GormToolsDataTester implements JsonifyUnitTest, GormToolsTestHelper, BuildDataTest, AutowiredTest{

    void mockDomains(Class<?>... domainClassesToMock) {
        BuildDataTest.super.mockDomains(domainClassesToMock)
        mockRepositories(domainClassesToMock)
    }

    @CompileDynamic
    def <T> T buildCreate(Map args = [:], Class<T> clazz, Map renderArgs = [:]) {
        Map p = buildMap(args, clazz, renderArgs)
        clazz.create(p)
    }

}
