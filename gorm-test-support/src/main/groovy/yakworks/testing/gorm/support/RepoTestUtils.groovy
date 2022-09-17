/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.support

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.util.ClassUtils

import gorm.tools.api.IncludesConfig
import gorm.tools.async.AsyncService
import gorm.tools.async.ParallelStreamTools
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.idgen.PooledIdGenerator
import gorm.tools.mango.DefaultMangoQuery
import gorm.tools.mango.MangoBuilder
import gorm.tools.metamap.services.MetaEntityService
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.model.UuidGormRepo
import gorm.tools.repository.model.UuidRepoEntity
import gorm.tools.transaction.TrxService
import grails.config.Config
import grails.core.GrailsApplication
import grails.persistence.support.NullPersistentContextInterceptor
import grails.spring.BeanBuilder
import yakworks.grails.GrailsHolder
import yakworks.i18n.icu.GrailsICUMessageSource
import yakworks.spring.AppCtx

/**
 * Helper utils for mocking spring beans needed to test repository's and domains.
 * if using alone and not as part of DataRepoTest or DomainRepoTest then
 *
    void setupSpec() {
        defineCommonGormBeans()
    }
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
@SuppressWarnings(["Indentation", "AssignmentToStaticFieldFromInstanceMethod"])
class RepoTestUtils {

    GrailsApplication grailsApplication

    RepoTestUtils(GrailsApplication grailsApplication){
        this.grailsApplication = grailsApplication
    }

    static RepoTestUtils init(GrailsApplication grailsApplication){
        def rtu = new RepoTestUtils(grailsApplication)
        //for some reason holder get scrambled so make sure it has the gapp we are using
        GrailsHolder.setGrailsApplication(grailsApplication)
        AppCtx.setApplicationContext(rtu.ctx)
        return rtu
    }

    ConfigurableApplicationContext getApplicationContext() {
        (ConfigurableApplicationContext) grailsApplication.mainContext
    }

    /** conveinince shortcut for applicationContext */
    ConfigurableApplicationContext getCtx() {
        getApplicationContext()
    }

    Config getConfig() {
        grailsApplication.config
    }

    @CompileDynamic
    Closure commonGormBeans(){ { ->
        // xmlns([context:"http://www.springframework.org/schema/context"])
        // context.'component-scan'('base-package': 'gorm.tools.settings')

        entityMapBinder(EntityMapBinder)
        repoEventPublisher(RepoEventPublisher)
        //repoUtilBean(RepoUtil)
        repoExceptionSupport(RepoExceptionSupport)
        mangoQuery(DefaultMangoQuery)
        mangoBuilder(MangoBuilder)
        trxService(TrxService)

        jdbcIdGenerator(MockJdbcIdGenerator)
        idGenerator(PooledIdGenerator, ref("jdbcIdGenerator"))
        persistenceContextInterceptor(NullPersistentContextInterceptor) //required for parallelTools
        parallelTools(ParallelStreamTools)
        asyncService(AsyncService)
        includesConfig(IncludesConfig)
        metaEntityService(MetaEntityService)
        metaMapService(MetaMapService)
        problemHandler(ProblemHandler)
        messageSource(GrailsICUMessageSource)
        externalConfigLoader(ExternalConfigLoader)
        // asyncProperties(AsyncProperties)
    }}

    @CompileDynamic
    Closure repoBeansClosure(Class<?>... domainClassesToMock){ { ->
        for(Class domainClass in domainClassesToMock){
            Class repoClass = findRepoClass(domainClass)
            grailsApplication.addArtefact(RepositoryArtefactHandler.TYPE, repoClass)
            String repoName = RepoUtil.getRepoBeanName(domainClass)
            if (repoClass == DefaultGormRepo || repoClass == UuidGormRepo) {
                "$repoName"(repoClass, domainClass)
            } else {
                "$repoName"(repoClass)
            }
        }
    }}

    /**
     * Finds repository class in same package as domain class.
     * returns a default DefaultGormRepo or UuidGormRepo if no explicit ones are found
     */
    Class findRepoClass(Class entityClass) {
        String repoClassName = RepositoryArtefactHandler.getRepoClassName(entityClass)
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
        if(UuidRepoEntity.isAssignableFrom(entityClass)) {
            return UuidGormRepo
        } else {
            return DefaultGormRepo
        }
    }

    /**
     * allows to pass in a list of bean closures, calling defineBeans sometimes causes problems so this allows
     * you to pass in the collection and do the defintion at one time
     */
    void defineBeansMany(List<Closure> closures) {
        def binding = new Binding()
        def bb = new BeanBuilder(null, null, grailsApplication.getClassLoader())
        binding.setVariable "application", grailsApplication
        bb.setBinding binding
        for(Closure closure : closures){
            if(closure) bb.beans(closure)
        }
        bb.registerBeans((BeanDefinitionRegistry)applicationContext)
        // applicationContext.beanFactory.preInstantiateSingletons()
    }

    /**
     * removes bean with name and replace with passed in object, defaults to autowired
     */
    @CompileDynamic
    void replaceSpringBean(String name, Object obj, boolean autowire = true){
        def beanInstance = autowire ? autowire(repo) : obj
        ctx.beanFactory.removeBeanDefinition(name)
        ctx.beanFactory.registerSingleton(name, beanInstance)
    }

    /**
     * Autowires bean properties for object
     */
    def autowire(Object obj) {
        ctx.autowireCapableBeanFactory.autowireBeanProperties(obj, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false)
        obj
    }

}
