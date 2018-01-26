package gorm.tools.testing

import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest
import groovy.transform.CompileStatic

@SuppressWarnings(['JUnitPublicNonTestMethod'])
@CompileStatic
trait GormToolsTest implements GormToolsTestHelper, DataTest, AutowiredTest {

    /**
     * Mocks domain classes providing the equivalent GORM behavior as well as the Repo for the domain.
     * If a Repository Class is explicitly defined then this looks for it in the same package
     *
     * @param domainClassesToMock The list of domain classes to mock
     */
    @Override
    void mockDomains(Class<?>... domainClassesToMock) {
        DataTest.super.mockDomains(domainClassesToMock)
        mockRepositories(domainClassesToMock)
    }

}
