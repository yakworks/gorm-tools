package gorm.tools.testing

import gorm.tools.dao.DaoUtil
import gorm.tools.dao.DefaultGormDao
import gorm.tools.dao.GormDao
import grails.test.hibernate.HibernateSpec
import grails.testing.spring.AutowiredTest
import org.grails.datastore.gorm.utils.ClasspathEntityScanner
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter

class DaoHibernateSpec extends HibernateSpec implements AutowiredTest  {

    void setupSpec() {
        DaoTestHelper.grailsApplication = grailsApplication
        List<Class> domainClasses = getDomainClasses()
        String packageName = getPackageToScan(config)
        Package packageToScan = Package.getPackage(packageName) ?: getClass().getPackage()

        Closure beans = {}

        if (domainClasses) {
            domainClasses.each { Class domainClass ->
                beans = beans << DaoTestHelper.registerDao(domainClass, DaoTestHelper.findDaoClass(domainClass))
            }
        }
        else {
            Set<Class> daoClasses = scanDaoClasses(packageName)
            //TODO figureout alternative to find entities if possible.
            new ClasspathEntityScanner().scan(packageToScan).each { Class domainClass ->
                Class daoClass = daoClasses.find{ it.simpleName == DaoUtil.getDaoBeanName(domainClass) } ?: DefaultGormDao
                beans = beans << DaoTestHelper.registerDao(domainClass, daoClass)
            }
        }

        beans = beans << DaoTestHelper.commonBeans()

        defineBeans(beans)
    }

    //scans all dao classes in given package.
    //may be change to DaoScanner like ClassPathEntityScanner !?
    @SuppressWarnings(['ClassForName'])
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
