package gorm.tools.testing

import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import grails.test.hibernate.HibernateSpec
import grails.testing.spring.AutowiredTest
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.utils.ClasspathEntityScanner
import org.grails.datastore.mapping.core.AbstractDatastore
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter

@CompileStatic
class GormToolsHibernateSpec extends HibernateSpec implements GormToolsTestHelper, AutowiredTest {

    void setupSpec() {
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

        beans = beans << commonBeans()

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

}
