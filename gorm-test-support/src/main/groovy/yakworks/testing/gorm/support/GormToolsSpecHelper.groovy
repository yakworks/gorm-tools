/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.support

import groovy.transform.CompileDynamic

import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.testing.GrailsUnitTest
import org.grails.testing.gorm.spock.DataTestSetupSpecInterceptor
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.util.ClassUtils
import org.springframework.validation.Validator

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
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.model.UuidGormRepo
import gorm.tools.repository.model.UuidRepoEntity
import gorm.tools.transaction.TrxService
import gorm.tools.validation.RepoEntityValidator
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
        defineCommonBeans()
    }
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileDynamic
@SuppressWarnings(["Indentation", "AssignmentToStaticFieldFromInstanceMethod"])
trait GormToolsSpecHelper extends GrailsUnitTest {

    // private static GrailsApplication _grailsApplication
    private static boolean _hasCommonBeansSetup = false

    /** conveinince shortcut for applicationContext */
    ConfigurableApplicationContext getCtx() {
        getApplicationContext()
    }

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

    Closure commonBeans(){ { ->
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
    }}

    /**
     * see commonBeans, sets up beans for common services for binders, mango, idegen, parralelTools and msgService
     */
    void defineCommonBeans(){
        if(!_hasCommonBeansSetup){
            defineBeans(commonBeans())
            _hasCommonBeansSetup = true
        }
    }

    void defineRepoBeans(Class<?>... domainClassesToMock){
        RepoLookup.USE_CACHE = false
        //for some reason holder get scrambled so make sure it has the gapp we are using
        GrailsHolder.setGrailsApplication(grailsApplication)
        AppCtx.setApplicationContext(getApplicationContext())

        Closure beanClos = {
            Collection<PersistentEntity> entities = datastore.mappingContext.persistentEntities
            for (Class domainClass in domainClassesToMock) {
                //do repo
                //Class domainClass = entity.javaClass
                Class repoClass = findRepoClass(domainClass)
                String beanName = RepoUtil.getRepoBeanName(domainClass)
                grailsApplication.addArtefact(RepositoryArtefactHandler.TYPE, repoClass)
                String repoName = RepoUtil.getRepoBeanName(domainClass)

                if (repoClass == DefaultGormRepo || repoClass == UuidGormRepo) {
                    "$repoName"(repoClass, domainClass, lazy())
                } else {
                    "$repoName"(repoClass, lazy())
                }
            }
        }
        //
        // Method doWithDomainsMethod = clazz.getMethod('getDoWithSpring')
        // defineBeans((Closure)doWithDomainsMethod.invoke(plugin))
        def beanClosures = [commonBeans(), beanClos]
        def doWithDomainsClosure = doWithDomains()
        if(doWithDomainsClosure) beanClosures.add(doWithDomainsClosure)

        //put here so we can use trait to setup security when needed
        def doWithSecurityClosure = doWithSecurity()
        if(doWithSecurityClosure) beanClosures.add(doWithSecurityClosure)

        defineBeansMany(beanClosures)

        //put here so we can use trait to setup security when needed
        doAfterDomains()

        // redo the cache for the repo event methods in the repos
        // ctx.getBean('repoEventPublisher').scanAndCacheEventsMethods()
        // ctx.getBean('repoEventPublisher').applicationEventPublisher = grailsApplication.mainContext
        // RepoValidatorRegistry.init(datastore, ctx.getBean('messageSource'))
    }

    Closure lazy() {{ bean ->
        bean.lazyInit = true
    }}

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
            bb.beans(closure)
        }
        bb.registerBeans((BeanDefinitionRegistry)applicationContext)
        applicationContext.beanFactory.preInstantiateSingletons()
    }

    @CompileDynamic
    void setupValidatorRegistry(){
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

    /**
     * removes bean with name and replace with passed in object, defaults to autowired
     */
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

    /**
     * override this to add beans during appContext init with the domains and repos
     */
    Closure doWithDomains() {
        null
    }

    /**
     * override this to add beans during appContext init with the domains and repos
     */
    void doAfterDomains() {
        null
    }

    /**
     * override this to add beans that are needed for security setups
     */
    Closure doWithSecurity() {
        null
    }
}