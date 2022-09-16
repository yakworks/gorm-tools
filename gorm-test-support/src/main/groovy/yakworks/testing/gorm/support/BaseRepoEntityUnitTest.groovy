/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.support

import groovy.transform.CompileDynamic

import org.grails.config.PropertySourcesConfig
import org.grails.datastore.mapping.core.AbstractDatastore
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.testing.gorm.spock.DataTestSetupSpecInterceptor
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.MessageSource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.validation.Validator

import gorm.tools.ConfigDefaults
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.validation.RepoEntityValidator
import gorm.tools.validation.RepoValidatorRegistry
import grails.config.Config
import grails.core.GrailsApplication
import yakworks.commons.lang.PropertyTools

/**
 * Base trait for mocking spring beans needed to test repository's and domain entities.
 * Serves as the foundation for GormHibernateSpec and DataRepoTest (SimpleMapDataStore)
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileDynamic
@SuppressWarnings(["Indentation", "AssignmentToStaticFieldFromInstanceMethod"])
trait BaseRepoEntityUnitTest {

    private RepoTestUtils _repoTestingUtils

    private boolean _hasCommonBeansSetup = false

    RepoTestUtils getRepoTestUtils(){
        if(!_repoTestingUtils) this._repoTestingUtils = RepoTestUtils.init(getGrailsApplication())
        return _repoTestingUtils
    }

    /**
     * The domain classes,
     * to be implemented in the unit test
     */
    // List<Class> getDomainClasses(){
    //     return []
    // }


    //---- from the GrailsUnitTest ---
    abstract Config getConfig()
    abstract ConfigurableApplicationContext getApplicationContext()
    abstract GrailsApplication getGrailsApplication()

    //---- implemented in the datastore tests (hibernate or simple)---
    AbstractDatastore getDatastore() {
        applicationContext.getBean(AbstractDatastore)
    }

    /** conveinince shortcut for applicationContext */
    ConfigurableApplicationContext getCtx() {
        getApplicationContext()
    }

    PlatformTransactionManager getTransactionManager() {
        applicationContext.getBean('transactionManager', PlatformTransactionManager)
    }

    Closure commonBeans(){
        return getRepoTestUtils().commonBeans()
    }

    //not relevant unless its in a hibernate spec
    Closure hibernateBeans() {
        null
    }

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
        RepoTestUtils gtu = getRepoTestUtils()
        RepoLookup.USE_CACHE = false

        Closure beanClos = gtu.repoBeansClosure(domainClassesToMock)

        def beanClosures = [commonBeans(), beanClos, hibernateBeans(), doWithDomains(), doWithSecurity()]
        getRepoTestUtils().defineBeansMany(beanClosures)

        // rescan needed after the beans are added
        ctx.getBean('repoEventPublisher', RepoEventPublisher).scanAndCacheEventsMethods()
        // this does something to make the events work
        applicationContext.beanFactory.preInstantiateSingletons()

        RepoValidatorRegistry.init(getDatastore(), ctx.getBean('messageSource', MessageSource))

        //put here so we can use trait to setup security when needed
        doAfterDomains()
    }


    // void doBinderConfigProps(){
    //     getApplicationContext()
    //     ConfigurationPropertySource source = new MapConfigurationPropertySource(
    //         TestPropertySourceUtils.convertInlinedPropertiesToMap(configuration.getPropertySourceProperties()))
    //     Binder binder = new Binder(source);
    //     def res = binder.bind("spring.main.web-application-type", Bindable.of(WebApplicationType.class))
    //         .orElseGet(this::deduceWebApplicationType)
    // }

    /**
     * looks for either domainClasses or entityClasses property on the test. can be either a static or a getter.
     */
    @CompileDynamic
    List<Class> findEntityClasses(){
        def persistentClasses = (PropertyTools.getOrNull(this, 'domainClasses')?:PropertyTools.getOrNull(this, 'entityClasses')) as List<Class>
        return persistentClasses
    }

    /**
     * replace method in DataTest, this is called from the gorm-testing's spock events classes, DataTestSetupSpecInterceptor
     */
    Class<?>[] getDomainClassesToMock() {
        def persistentClasses = findEntityClasses()
        return (persistentClasses?:[]) as Class<?>[]
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

    /**
     * override this to modify config
     */
    @CompileDynamic //closure has weird delgate if its not CompileDynamic
    Closure doWithConfig() {
        { cfg ->
            gormConfigDefaults((PropertySourcesConfig)cfg)
        }
    }

    PropertySourcesConfig gormConfigDefaults(PropertySourcesConfig config){
        config.putAll(ConfigDefaults.getConfigMap(false))
        return config
    }

}
