package gorm.tools.testing

import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest
import groovy.transform.CompileStatic

@SuppressWarnings(['JUnitPublicNonTestMethod'])
@CompileStatic
trait DaoDataTest implements DaoTestHelper, DataTest, AutowiredTest {

    /**
     * Mocks domain classes providing the equivalent GORM behavior as well as the Dao for the domain.
     * If a Dao Class is explicitly defined then this looks for it in the same package
     *
     * @param domainClassesToMock The list of domain classes to mock
     */
    void mockDomains(Class<?>... domainClassesToMock) {

        DataTest.super.mockDomains(domainClassesToMock)

        Closure daoBeans = {}

        domainClassesToMock.each { Class domainClass ->
            Class daoClass = findDaoClass(domainClass)
            daoBeans = daoBeans << registerDao(domainClass, daoClass)
        }

        defineBeans(daoBeans << commonBeans())
    }

}
