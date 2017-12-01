package gorm.tools.testing

import gorm.tools.dao.DaoEventInvoker
import gorm.tools.dao.DaoUtil
import gorm.tools.dao.DefaultGormDao
import gorm.tools.databinding.FastBinder
import grails.plugin.dao.DaoArtefactHandler
import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest
import grails.util.GrailsNameUtils
import org.springframework.util.ClassUtils

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
                Class daoClass = Class.forName(daoClassName)
                final daoArtefact = grailsApplication.addArtefact(DaoArtefactHandler.TYPE, daoClass)
                daoBeans = daoBeans << {
                    "${daoArtefact.propertyName}"(daoClass) { bean -> bean.autowire = true }
                }
            } else{
                String daoName = "${GrailsNameUtils.getPropertyName(domainClass.name)}Dao"
                daoBeans = daoBeans << {
                    "${daoName}"(DefaultGormDao, domainClass) { bean -> bean.autowire = true }
                }
            }
        }

        defineBeans(daoBeans << {
            fastBinder(FastBinder)
            daoEventInvoker(DaoEventInvoker)
            daoUtilBean(DaoUtil)
        })
    }

    void mockDao(Class daoClass) {
        //println "mocking dao $daoClass"
        final daoArtefact = grailsApplication.addArtefact(DaoArtefactHandler.TYPE, daoClass)
        registerBeanIfRequired(daoArtefact.propertyName, daoClass)
    }

    void registerBeanIfRequired(String name, Class clazz, autowire = true) {
        if(!applicationContext.containsBean(name)) {
            defineBeans({
                "$name"(clazz) {bean ->
                    bean.autowire = autowire
                }
            })
        }
    }



}
