/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.support

import groovy.transform.CompileDynamic

import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.testing.GrailsUnitTest
import org.grails.testing.gorm.spock.DataTestSetupSpecInterceptor
import org.junit.BeforeClass
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.util.ClassUtils
import org.springframework.validation.Validator

import gorm.tools.databinding.EntityMapBinder
import gorm.tools.idgen.PooledIdGenerator
import gorm.tools.mango.DefaultMangoQuery
import gorm.tools.mango.MangoBuilder
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.validation.RepoEntityValidator
import gorm.tools.transaction.TrxService

/**
 * Helper utils for mocking spring beans needed to test repository's and domains.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileDynamic
//@SuppressWarnings(["ClosureAsLastMethodParameter"])
trait GormToolsSpecHelper extends GrailsUnitTest {

    @BeforeClass
    void setupTransactionService() {
        defineBeans {
            trxService(TrxService)
        }
    }

    /**
     * Mocks Repositories for passed in Domain classes.
     * If a Repository Class is explicitly defined then this looks for it in the same package
     * The domains should be mocked before this is called
     */
    void mockRepositories(Class<?>... domainClassesToMock) {

        Closure repoBeans = {}

        domainClassesToMock.each { Class domainClass ->
            Class repoClass = findRepoClass(domainClass)
            repoBeans = repoBeans << registerRepository(domainClass, repoClass)
        }
        defineBeans(repoBeans << commonBeans())

        //This part is needed because we cache repo entity in domain,
        //so if we have 2 unit tests where we create same bean for repo, then in second
        //test class when we call repo from domain(it could be Org.repo or Org.create()) we will call repo from
        //the first test class, and if we injected new dependencies in can break test
        // if (this.hasProperty("entityClass") && this.entityClass){
        //     domainClassesToMock = [this.entityClass].toArray(Class) + domainClassesToMock
        // }
        // domainClassesToMock.each {
        //     String repoBeanName = RepoUtil.getRepoBeanName(it)
        //     GormRepo repo = AppCtx.get("${repoBeanName}", findRepoClass(it))
        //     // it.setRepo(repo)
        // }

    }

    /** conveinince shortcut for applicationContext */
    ConfigurableApplicationContext getCtx() {
        getApplicationContext()
    }

    @CompileDynamic
    Closure commonBeans() {
        return {
            entityMapBinder(EntityMapBinder, grailsApplication)
            repoEventPublisher(RepoEventPublisher)
            repoUtilBean(RepoUtil)
            repoExceptionSupport(RepoExceptionSupport)
            mangoQuery(DefaultMangoQuery)
            mangoBuilder(MangoBuilder)
            trxService(TrxService)

            jdbcIdGenerator(MockJdbcIdGenerator)
            idGenerator(PooledIdGenerator, ref("jdbcIdGenerator"))
        }
    }

    /**
     * Finds repository class in same package as domain class.
     * returns a default DefaultGormRepo if no explicit ones are found
     */
    Class findRepoClass(Class entityClass) {
        String repoClassName = RepoUtil.getRepoClassName(entityClass)
        //println "finding $repoClassName"
        if (ClassUtils.isPresent(repoClassName, grailsApplication.classLoader)) {
            return ClassUtils.forName(repoClassName)
        }
        // if its can't be found in same package then see if entityClass has the getRepo static
        def mbp = entityClass.metaClass.properties.find{ it.name == 'repo'} as MetaBeanProperty
        Class repoClass = mbp?.getter?.returnType
        if(repoClass && repoClass != GormRepo){
            return repoClass
        }
        return DefaultGormRepo
    }

    @CompileDynamic
    Closure registerRepository(Class domain, Class repoClass) {
        String beanName = RepoUtil.getRepoBeanName(domain)
        grailsApplication.addArtefact(RepositoryArtefactHandler.TYPE, repoClass)
        Closure clos = { "$beanName"(repoClass) { bean -> bean.autowire = true } }

        if (repoClass == DefaultGormRepo) {
            clos = { "$beanName"(repoClass, domain) { bean -> bean.autowire = true } }
        }

        return clos
    }

    /**
     * No Usages Yet..., scans all repository classes in given package.
     * may be change to RepoScanner like ClassPathEntityScanner !?
     */
    @SuppressWarnings(['ClassForName'])
    Set<Class> scanRepoClasses(String packageName) {
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
    void setupValidatorRegistry(){
        // def settings = datastore.getConnectionSources().getDefaultConnectionSource().getSettings()
        // def mappingContext = datastore.mappingContext
        // def validatorRegistry = new RepoValidatorRegistry(mappingContext, settings, getCtx())
        // mappingContext.setValidatorRegistry(validatorRegistry)

        Collection<PersistentEntity> entities = datastore.mappingContext.persistentEntities
        for (PersistentEntity entity in entities) {
            Validator validator = registerDomainClassValidator(entity)

            datastore.mappingContext.addEntityValidator(entity, validator)
        }
    }

    @CompileDynamic
    private Validator registerDomainClassValidator(PersistentEntity domain) {
        String validationBeanName = "${domain.javaClass.name}Validator"
        defineBeans {
            "$validationBeanName"(RepoEntityValidator, domain, ref("messageSource"), ref(DataTestSetupSpecInterceptor.BEAN_NAME))
        }

        applicationContext.getBean(validationBeanName, Validator)
    }

    void flush() {
        getDatastore().currentSession.flush()
    }

    void flushAndClear(){
        getDatastore().currentSession.flush()
        getDatastore().currentSession.clear()
    }
}
