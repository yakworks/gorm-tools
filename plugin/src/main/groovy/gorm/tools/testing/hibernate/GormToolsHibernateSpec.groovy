package gorm.tools.testing.hibernate

import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.testing.GormToolsTestHelper
import gorm.tools.testing.JsonifyUnitTest
import grails.buildtestdata.TestDataBuilder
import grails.plugin.gormtools.GormToolsPluginHelper
import grails.test.hibernate.HibernateSpec
import groovy.transform.CompileDynamic
import org.grails.datastore.gorm.utils.ClasspathEntityScanner
import org.grails.datastore.mapping.core.AbstractDatastore
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter

@SuppressWarnings(['AbstractClassWithoutAbstractMethod'])
@CompileDynamic
abstract class GormToolsHibernateSpec extends HibernateSpec implements JsonifyUnitTest, TestDataBuilder, GormToolsTestHelper {

    void setupSpec() {
        if (!ctx.containsBean("dataSource"))
            ctx.beanFactory.registerSingleton("dataSource", hibernateDatastore.getDataSource())
//        if (!ctx.containsBean("transactionService"))
//            ctx.beanFactory.registerSingleton("transactionService", datastore.getService(TransactionService))
        if (!ctx.containsBean("grailsDomainClassMappingContext"))
            ctx.beanFactory.registerSingleton("grailsDomainClassMappingContext", hibernateDatastore.getMappingContext())

        List<Class> domainClasses = getDomainClasses()
        String packageName = getPackageToScan(config)
        Package packageToScan = Package.getPackage(packageName) ?: getClass().getPackage()

        Closure beans = {}

        if (domainClasses) {
            domainClasses.each { Class domainClass ->
                beans = beans << registerRepository(domainClass, findRepoClass(domainClass))
            }
        } else {
            Set<Class> repoClasses = scanRepoClasses(packageName)
            //TODO figureout alternative to find entities if possible.
            new ClasspathEntityScanner().scan(packageToScan).each { Class domainClass ->
                Class repoClass = repoClasses.find {
                    it.simpleName == RepoUtil.getRepoBeanName(domainClass)
                } ?: DefaultGormRepo
                beans = beans << registerRepository(domainClass, repoClass)
            }
        }

        beans = beans << GormToolsPluginHelper.doWithSpring //commonBeans()
        defineBeans(beans)
    }

    @CompileDynamic
    AbstractDatastore getDatastore() {
        hibernateDatastore
    }

    //scans all repository classes in given package.
    //may be change to RepoScanner like ClassPathEntityScanner !?
    @SuppressWarnings(['ClassForName'])
    protected Set<Class> scanRepoClasses(String packageName) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false)
        provider.addIncludeFilter(new AssignableTypeFilter(GormRepo))
        Set<BeanDefinition> beans = provider.findCandidateComponents(packageName)

        Set<Class> repoClasses = []
        for (BeanDefinition bd : beans) {
            repoClasses << Class.forName(bd.beanClassName, false, grailsApplication.classLoader)
        }
        return repoClasses
    }

    @CompileDynamic
    def <T> T buildCreate(Map args = [:], Class<T> clazz, Map renderArgs = [:]) {
        Map p = buildJson(args, clazz, renderArgs).json as Map
        clazz.create(p)
    }

}
