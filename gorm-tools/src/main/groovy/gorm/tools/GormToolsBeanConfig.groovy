/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.mapping.model.MappingContext
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.orm.hibernate.connections.HibernateConnectionSourceSettings
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.api.ProblemHandler
import gorm.tools.async.AsyncService
import gorm.tools.async.ParallelStreamTools
import gorm.tools.beans.EntityMapService
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.idgen.JdbcIdGenerator
import gorm.tools.idgen.PooledIdGenerator
import gorm.tools.jdbc.DbDialectService
import gorm.tools.mango.DefaultMangoQuery
import gorm.tools.mango.MangoBuilder
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.artefact.GrailsRepositoryClass
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.validation.RepoValidatorRegistry
import gorm.tools.support.MsgService
import gorm.tools.transaction.TrxService
import grails.config.Config
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.Plugin

/**
 * main area to setup beans with the bean builder
 */
@Slf4j
@CompileDynamic //ok
@SuppressWarnings(['Println', 'Indentation'])
class GormToolsBeanConfig {
    Config config
    ConfigurableApplicationContext applicationContext

    GormToolsBeanConfig(ConfigurableApplicationContext appCtx){
        applicationContext = appCtx
    }

    GormToolsBeanConfig(Config config, ConfigurableApplicationContext appCtx){
        this.config = config
        applicationContext = appCtx
    }

    Closure getBeanDefinitions() {{->
        msgService(MsgService)
        jdbcTemplate(JdbcTemplate, ref("dataSource"), lazy())

        jdbcIdGenerator(JdbcIdGenerator) { bean ->
            bean.lazyInit = true
            jdbcTemplate = ref('jdbcTemplate')
            table = "NewObjectId"
            keyColumn = "KeyName"
            idColumn = "NextId"
        }

        idGenerator(PooledIdGenerator, jdbcIdGenerator, lazy())

        mangoQuery(DefaultMangoQuery, lazy())
        mangoBuilder(MangoBuilder, lazy())

        entityMapBinder(EntityMapBinder, ref('grailsApplication'), lazy())
        entityMapService(EntityMapService, lazy())

        repoEventPublisher(RepoEventPublisher)

        repoExceptionSupport(RepoExceptionSupport, lazy())

        parallelTools(ParallelStreamTools, lazy())

        asyncService(AsyncService, lazy())

        DbDialectService.dialectName = application.config.hibernate.dialect

        dbDialectService(DbDialectService) { bean ->
            bean.lazyInit = true
            jdbcTemplate = ref('jdbcTemplate')
        }

        trxService(TrxService, lazy())
        problemErrorHandler(ProblemHandler, lazy())

        def repoClasses = application.repositoryClasses
        for(GrailsRepositoryClass repoClass : repoClasses){
            def beanClosure = getRepoBeanClosure(repoClass)
            beanClosure.delegate = delegate
            beanClosure()
        }

        for (GrailsClass grailsClass in application.getArtefacts(DomainClassArtefactHandler.TYPE)) {
            final domainClass = grailsClass.clazz

            // make sure each domain has a repository, if not set up a DefaultGormRepo for it.
            String repoName = RepoUtil.getRepoBeanName(domainClass)
            def hasRepo = repoClasses.find { it.propertyName == repoName }
            if (!hasRepo) {
                "${repoName}"(DefaultGormRepo, domainClass) { bean ->
                    bean.autowire = true
                    bean.lazyInit = true
                }
            }
        }
    }}

    //This is kind of equivalent to init in bootstrap
    @CompileStatic
    void doWithApplicationContext() {
        //FIXME, this make it very dependant on hibernate
        //connect up the gorm-tools validator registry that fixes events and allows openapi schema
        HibernateDatastore datastore = applicationContext.getBean("hibernateDatastore", HibernateDatastore)
        MappingContext mappingContext = applicationContext.getBean("grailsDomainClassMappingContext", MappingContext)
        def origValRegistry = mappingContext.getValidatorRegistry()
        HibernateConnectionSourceSettings settings = datastore.getConnectionSources().getDefaultConnectionSource().getSettings()
        def validatorRegistry = new RepoValidatorRegistry(mappingContext, settings, origValRegistry.messageSource)
        mappingContext.setValidatorRegistry(validatorRegistry)
    }

    static Closure getRepoBeanClosure(GrailsRepositoryClass repoClass) {
        def lazyInit = repoClass.hasProperty("lazyInit") ? repoClass.getPropertyValue("lazyInit") : true

        Closure bClosure = {
            "${repoClass.propertyName}"(repoClass.getClazz()) { bean ->
                bean.autowire = true
                bean.lazyInit = lazyInit
            }
        }
        return bClosure
    }

    static void onChange(Object event, GrailsApplication grailsApplication, Plugin plugin) {
        if (!event.source || !event.ctx) {
            return
        }
        if (grailsApplication.isArtefactOfType(RepositoryArtefactHandler.TYPE, event.source)) {

            GrailsClass repoClass = grailsApplication.addArtefact(RepositoryArtefactHandler.TYPE, event.source)

            plugin.beans(getRepoBeanClosure(repoClass))
        }
        //registryRestApiControllers(grailsApplication)
    }

    // Closure autowireLazy() {{ bean ->
    //     bean.lazyInit = true
    //     bean.autowire = true
    // }}

    Closure lazy() {{ bean ->
        bean.lazyInit = true
    }}

    void registerBeans(Closure beanClosure, Object delegate) {
        beanClosure.delegate = delegate
        beanClosure()
    }

}
