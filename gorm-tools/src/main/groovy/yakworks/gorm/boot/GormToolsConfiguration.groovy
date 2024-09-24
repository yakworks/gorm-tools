/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.boot

import javax.annotation.PostConstruct
import javax.sql.DataSource

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.AbstractMappingContext
import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.async.AsyncService
import gorm.tools.async.ParallelStreamTools
import gorm.tools.async.ParallelTools
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
import gorm.tools.validation.RepoValidatorRegistry
import yakworks.gorm.api.ApiConfig
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.support.DefaultQueryArgsValidator
import yakworks.gorm.api.support.QueryArgsValidator
import yakworks.gorm.config.GormToolsPropertiesConfiguration

@AutoConfiguration
@Lazy
@Import([GormToolsPropertiesConfiguration]) //Config Props AsyncConfig, GormConfig, IdGeneratorConfig, QueryConfig
// @AutoConfigureBefore([HibernateJpaAutoConfiguration])
// @AutoConfigureAfter(DataSourceAutoConfiguration)
@CompileStatic
class GormToolsConfiguration {

    @Autowired HibernateDatastore hibernateDatastore
    @Autowired MessageSource messageSource

    // see https://zetcode.com/spring/beanfactorypostprocessor/ for lambda BeanFactoryPostProcessor
    @Bean
    //important here, if we dont do DependsOn then it eagerly instantiates the DataSource before its ready.
    //@ConditionalOnBean(HibernateDatastore)
    @DependsOn(["grailsDomainClassMappingContext", "appCtx"])
    static GormRepoBeanFactoryPostProcessor gormRepoBeanFactoryPostProcessor(AbstractMappingContext grailsDomainClassMappingContext) {
        return new GormRepoBeanFactoryPostProcessor(grailsDomainClassMappingContext)
    }

    @PostConstruct
    void init(){
        //register the gorm validators
        RepoValidatorRegistry.init(hibernateDatastore, messageSource)
    }

    @Bean
    @ConditionalOnMissingBean
    ApiConfig apiConfig(){
        new ApiConfig()
    }

    @Bean
    @ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
    MangoBuilder mangoBuilder(){ new MangoBuilder()}

    @Bean
    @ConditionalOnMissingBean
    EntityMapBinder entityMapBinder(){ new EntityMapBinder()}

    @Bean
    @ConditionalOnMissingBean
    MetaEntityService metaEntityService(){ new MetaEntityService()}

    @Bean
    @ConditionalOnMissingBean
    MetaMapService metaMapService(){ new MetaMapService()}

    @Bean
    @ConditionalOnMissingBean
    RepoEventPublisher repoEventPublisher(){ new RepoEventPublisher()}

    @Bean
    @ConditionalOnMissingBean
    RepoExceptionSupport repoExceptionSupport(){ new RepoExceptionSupport()}

    @Bean
    @ConditionalOnMissingBean
    ParallelTools parallelTools(){ new ParallelStreamTools()}

    @Bean
    @ConditionalOnMissingBean
    AsyncService asyncService(){ new AsyncService()}

    @Bean @Lazy(false)
    DbDialectService dbDialectService(){ new DbDialectService()}

    @Bean
    @ConditionalOnMissingBean
    TrxService trxService(){ new TrxService()}

    @Bean @Lazy(false)
    @ConditionalOnMissingBean
    ProblemHandler problemHandler(){
        new ProblemHandler()
    }

    @Bean
    @ConditionalOnMissingBean
    QueryArgsValidator queryArgsValidator(){
        new DefaultQueryArgsValidator()
    }
}
