package gorm.tools.testing

import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest

@SuppressWarnings(['JUnitPublicNonTestMethod'])
trait DaoDataTest implements DataTest, AutowiredTest {

    /**
     * Mocks domain classes providing the equivalent GORM behavior as well as the Dao for the domain.
     * If a Dao Class is explicitly defined then this looks for it in the same package
     *
     * @param domainClassesToMock The list of domain classes to mock
     */
    void mockDomains(Class<?>... domainClassesToMock) {
        DaoTestHelper.grailsApplication = grailsApplication

        DataTest.super.mockDomains(domainClassesToMock)

        Closure daoBeans = {}

        domainClassesToMock.each { Class domainClass ->
            Class daoClass = DaoTestHelper.findDaoClass(domainClass)
            daoBeans = daoBeans << DaoTestHelper.registerDao(domainClass, daoClass)
        }

        defineBeans(daoBeans << DaoTestHelper.commonBeans())
    }

}
