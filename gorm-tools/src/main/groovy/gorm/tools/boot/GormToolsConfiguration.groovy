/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.boot

import javax.sql.DataSource

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.AbstractMappingContext
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Lazy
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.api.IncludesConfig
import gorm.tools.async.AsyncService
import gorm.tools.async.ParallelStreamTools
import gorm.tools.async.ParallelTools
import gorm.tools.config.AsyncConfig
import gorm.tools.config.GormConfig
import gorm.tools.config.IdGeneratorConfig
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.idgen.IdGenerator
import gorm.tools.idgen.JdbcIdGenerator
import gorm.tools.idgen.PooledIdGenerator
import gorm.tools.jdbc.DbDialectService
import gorm.tools.mango.DefaultMangoQuery
import gorm.tools.mango.MangoBuilder
import gorm.tools.mango.api.MangoQuery
import gorm.tools.metamap.services.MetaEntityService
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.transaction.TrxService

@Configuration(proxyBeanMethods = false)
@Lazy
// @ComponentScan(['gorm.tools.config'])
// @ConfigurationPropertiesScan
@EnableConfigurationProperties([AsyncConfig, GormConfig, IdGeneratorConfig])
// @Import([AsyncConfig, GormConfig, IdGeneratorConfig])
@CompileStatic
class GormToolsConfiguration {

    // GrailsApplication grailsApplication

    // GormToolsConfiguration(GrailsApplication grailsApplication){
    //     this.grailsApplication = grailsApplication
    // }

    @Bean
    @DependsOn("grailsDomainClassMappingContext") //important here, if we dont do DependsOn then it eagerly instantiates the DataSource before its ready.
    GormRepoBeanFactoryPostProcessor gormRepoBeanFactoryPostProcessor(AbstractMappingContext grailsDomainClassMappingContext) {
        //AbstractMappingContext grailsDomainClassMappingContext
        // List<Class> repoClasses = grailsApplication.getArtefacts("Repository")*.clazz
        // List<Class> entityClasses = grailsApplication.getArtefacts("Domain")*.clazz
        // return new GormRepoBeanFactoryPostProcessor(entityClasses)
        return new GormRepoBeanFactoryPostProcessor(grailsDomainClassMappingContext)
    }

    @Bean
    IncludesConfig includesConfig(){ new IncludesConfig()}

    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource){
        new JdbcTemplate(dataSource)
    }

    @Bean
    JdbcIdGenerator jdbcIdGenerator(){ new JdbcIdGenerator() }

    @Bean
    IdGenerator idGenerator(JdbcIdGenerator jdbcIdGenerator){ new PooledIdGenerator(jdbcIdGenerator)}

    @Bean
    MangoQuery mangoQuery(){ new DefaultMangoQuery()}

    @Bean
    MangoBuilder mangoBuilder(){ new MangoBuilder()}

    @Bean
    EntityMapBinder entityMapBinder(){ new EntityMapBinder()}

    @Bean
    MetaEntityService metaEntityService(){ new MetaEntityService()}

    @Bean
    MetaMapService metaMapService(){ new MetaMapService()}

    @Bean
    RepoEventPublisher repoEventPublisher(){ new RepoEventPublisher()}

    @Bean
    RepoExceptionSupport repoExceptionSupport(){ new RepoExceptionSupport()}

    @Bean
    ParallelTools parallelTools(){ new ParallelStreamTools()}

    @Bean
    AsyncService asyncService(){ new AsyncService()}

    @Bean @Lazy(false)
    DbDialectService dbDialectService(){ new DbDialectService()}

    @Bean
    TrxService trxService(){ new TrxService()}

    @Bean
    ProblemHandler problemHandler(){ new ProblemHandler()}

}
