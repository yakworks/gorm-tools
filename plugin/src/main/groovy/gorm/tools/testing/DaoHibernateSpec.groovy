package gorm.tools.testing

import gorm.tools.dao.DaoEventInvoker
import gorm.tools.dao.DaoUtil
import gorm.tools.dao.DefaultGormDao
import gorm.tools.dao.GormDao
import gorm.tools.databinding.FastBinder
import grails.plugin.dao.DaoArtefactHandler
import grails.test.hibernate.HibernateSpec
import grails.util.GrailsNameUtils
import org.grails.datastore.gorm.utils.ClasspathEntityScanner
import org.grails.testing.GrailsUnitTest
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.util.ClassUtils

class DaoHibernateSpec extends HibernateSpec implements  GrailsUnitTest {

    void setupSpec() {
        List<Class> domainClasses = getDomainClasses()
        String packageName = getPackageToScan(config)
        Package packageToScan = Package.getPackage(packageName) ?: getClass().getPackage()

        Closure beans = { }

        if (!domainClasses) {
            Set<Class> daoClasses = scanDaoClasses(packageName)
            //TODO figureout alternative to find entities if possible.
            new ClasspathEntityScanner().scan(packageToScan).each { Class domainClass ->
                Class daoClass = daoClasses.find({ it.simpleName == "${domainClass.simpleName}Dao"}) ?: DefaultGormDao
                beans = beans << registerDao(domainClass, daoClass)
            }
        }
        else {
            domainClasses.each {Class domainClass ->
                beans = beans << registerDao(domainClass, findDao(domainClass))
            }
        }

        beans = beans << {
            fastBinder(FastBinder)
            daoEventInvoker(DaoEventInvoker)
            daoUtilBean(DaoUtil)
        }

        defineBeans(beans)
    }


    Closure registerDao(Class domain, Class daoClass) {
        String beanName = "${GrailsNameUtils.getPropertyName(domain.name)}Dao"
        grailsApplication.addArtefact(DaoArtefactHandler.TYPE, daoClass)
        return  {
            "$beanName"(daoClass) { bean-> bean.autowire = true}
        }

    }


    //may be change to DaoScanner like ClassPathEntityScanner !?
    protected Class<GormDao> findDao(Class domainClass) {
        Class<GormDao> daoClass
        String daoClassName = "${domainClass.name}Dao"
        if(ClassUtils.isPresent(daoClassName, grailsApplication.classLoader)){
            daoClass = ClassUtils.forName(daoClassName)// Class.forName(daoClassName)
        } else{
           return DefaultGormDao
        }
    }

    protected Set<Class> scanDaoClasses(String packageName) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false)
        provider.addIncludeFilter(new AssignableTypeFilter(GormDao))
        Set<BeanDefinition> beans = provider.findCandidateComponents(packageName)

        Set<Class> daoClasses = []
        for (BeanDefinition bd : beans) {
           daoClasses << Class.forName(bd.beanClassName, false, grailsApplication.classLoader)
        }

        return daoClasses
    }

}
