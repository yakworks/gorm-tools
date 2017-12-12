package gorm.tools.testing

import gorm.tools.TrxService
import gorm.tools.dao.DaoUtil
import gorm.tools.dao.DefaultGormDao
import gorm.tools.dao.events.DaoEventPublisher
import gorm.tools.databinding.FastBinder
import grails.plugin.dao.DaoArtefactHandler
import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest
import grails.util.GrailsNameUtils
import org.springframework.util.ClassUtils

@SuppressWarnings(['JUnitPublicNonTestMethod'])
trait DaoDataTest implements DataTest, AutowiredTest {

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
            String daoClassName = "${domainClass.name}Dao"
            if(ClassUtils.isPresent(daoClassName, grailsApplication.classLoader)){
                Class daoClass = ClassUtils.forName(daoClassName)// Class.forName(daoClassName)
                registerGormDaoClass(daoClass)
                daoBeans = daoBeans << {
                    "${GrailsNameUtils.getPropertyName(daoClass.name)}"(daoClass) { bean -> bean.autowire = true }
                }
            } else{
                String daoName = "${GrailsNameUtils.getPropertyName(domainClass.name)}Dao"
                daoBeans = daoBeans << {
                    "${daoName}"(DefaultGormDao, domainClass) { bean -> bean.autowire = true }
                }
            }
        }

        defineBeans(daoBeans << {
            trxService(TrxService)
            fastBinder(FastBinder)
            daoEventInvoker(DaoEventPublisher)
            daoUtilBean(DaoUtil)
        })
    }

    private void registerGormDaoClass(Class daoClass) {
        grailsApplication.addArtefact(DaoArtefactHandler.TYPE, daoClass)
    }

}
