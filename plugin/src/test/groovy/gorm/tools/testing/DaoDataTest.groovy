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

        registerBeanIfRequired("fastBinder", FastBinder)

        domainClassesToMock.each { Class domainClass ->
            String daoClassName = "${domainClass.name}Dao"
            println daoClassName
            if(ClassUtils.isPresent(daoClassName, grailsApplication.classLoader)){
                mockDao(Class.forName(daoClassName))
            } else{
                String daoName = "${GrailsNameUtils.getPropertyName(domainClass.name)}Dao"
                defineBeans({
                    "${daoName}"(DefaultGormDao, domainClass) { bean ->
                        bean.autowire = true
                        bean.lazyInit = true
                    }
                })
            }
        }
        defineBeans({
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
