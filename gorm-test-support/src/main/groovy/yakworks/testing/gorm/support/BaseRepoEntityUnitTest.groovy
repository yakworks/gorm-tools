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

    Closure commonGormBeans(){
        return getRepoTestUtils().commonGormBeans()
    }

    //not relevant unless its in a hibernate spec
    Closure hibernateBeans() {
        null
    }

    /**
     * see commonGormBeans, sets up beans for common services for binders, mango, idegen, parralelTools and msgService
     */
    void defineCommonGormBeans(){
        if(!_hasCommonBeansSetup){
            defineBeans(commonGormBeans())
            _hasCommonBeansSetup = true
        }
    }

    void defineRepoBeans(Class<?>... domainClassesToMock){
        RepoTestUtils gtu = getRepoTestUtils()
        RepoLookup.USE_CACHE = false

        Closure beanClos = gtu.repoBeansClosure(domainClassesToMock)

        def beanClosures = [commonGormBeans(), beanClos, hibernateBeans(), doWithGormBeans(), doWithSecurityBeans()]
        getRepoTestUtils().defineBeansMany(beanClosures)

        // rescan needed after the beans are added
        ctx.getBean('repoEventPublisher', RepoEventPublisher).scanAndCacheEventsMethods()
        // this does something to make the events work
        applicationContext.beanFactory.preInstantiateSingletons()

        RepoValidatorRegistry.init(getDatastore(), ctx.getBean('messageSource', MessageSource))

        //put here so we can use trait to setup security when needed
        doAfterGormBeans()
    }

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
    @Override //in the DataTest
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
     * doWithSpring is what to implement if beans are needed when it first creates the AppContext
     * override this to add beans during appContext init with the domains and repos
     * note: Often times strange things can happen if the implementation is not marked with @CompileDynamic when using @CompileStatic at class
     * @return the bean builder closure.
     */
    Closure doWithGormBeans() {
        null
    }

    /**
     * override this to be run after the doWithGormBeans along with the defaults have been added to the AppContext
     * @return the bean builder closure.
     */
    void doAfterGormBeans() {
        null
    }

    /**
     * override this to add beans that are needed for security setups.
     * just bean definitions that are added along with whats in doWithGormBeans
     * @return the bean builder closure.
     */
    Closure doWithSecurityBeans() {
        null
    }

    /**
     * doWithConfig is called early on the first setup of the AppCtx in the GrailsAppUnitTest.
     * override this to modify config but dont forget to add in the gormConfigDefaults if doing so.
     */
    @Override //in the GrailUnitTest
    @CompileDynamic //closure might have a weird delegate if this is not marked with CompileDynamic
    Closure doWithConfig() {
        { cfg ->
            gormConfigDefaults((PropertySourcesConfig)cfg)
        }
    }

    /**
     * helper to make it easy to add the ConfigDefaults if overriding, can also completely eliminate it too.
     */
    PropertySourcesConfig gormConfigDefaults(PropertySourcesConfig config){
        config.putAll(ConfigDefaults.getConfigMap(false))
        return config
    }

}
