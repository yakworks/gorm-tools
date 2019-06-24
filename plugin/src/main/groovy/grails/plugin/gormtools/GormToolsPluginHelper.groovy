/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package grails.plugin.gormtools

import groovy.transform.CompileDynamic

import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.DbDialectService
import gorm.tools.GormMetaUtils
import gorm.tools.async.GparsAsyncSupport
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.idgen.JdbcIdGenerator
import gorm.tools.idgen.PooledIdGenerator
import gorm.tools.mango.DefaultMangoQuery
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.support.MsgService
import grails.core.ArtefactHandler
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.Plugin

@SuppressWarnings(['UnnecessarySelfAssignment'])
@CompileDynamic
class GormToolsPluginHelper {
    static List<ArtefactHandler> artefacts = [new RepositoryArtefactHandler()]

    static Closure doWithSpring = {

        msgService(MsgService)

        jdbcTemplate(JdbcTemplate, ref("dataSource"))

        jdbcIdGenerator(JdbcIdGenerator) {
            jdbcTemplate = jdbcTemplate
            table = "NewObjectId"
            keyColumn = "KeyName"
            idColumn = "NextId"
        }

        idGenerator(PooledIdGenerator, jdbcIdGenerator)

        mangoQuery(DefaultMangoQuery)

        entityMapBinder(EntityMapBinder, ref('grailsApplication'))

        repoEventPublisher(RepoEventPublisher)

        repoExceptionSupport(RepoExceptionSupport)

        asyncSupport(GparsAsyncSupport)
//            { bean ->
//            bean.autowire = true
//            bean.lazyInit = true
//        }

        DbDialectService.dialectName = application.config.hibernate.dialect

        def repoClasses = application.repositoryClasses
        repoClasses.each { repoClass ->
            getRepoBeanClosure(repoClass, delegate).call()
        }

        //make sure each domain has a repository, if not set up a DefaultGormRepo for it.
        Class[] domainClasses = application.domainClasses*.clazz
        domainClasses.each { Class domainClass ->
            String repoName = RepoUtil.getRepoBeanName(domainClass)
            def hasRepo = repoClasses.find { it.propertyName == repoName }
            if (!hasRepo) {
                "${repoName}"(DefaultGormRepo, domainClass) { bean ->
                    bean.autowire = true
                    bean.lazyInit = true
                }
            }
        }
    }

    static void onChange(Object event, GrailsApplication grailsApplication, Plugin plugin) {
        if (!event.source || !event.ctx) {
            return
        }
        if (grailsApplication.isArtefactOfType(RepositoryArtefactHandler.TYPE, event.source)) {

            GrailsClass repoClass = grailsApplication.addArtefact(RepositoryArtefactHandler.TYPE, event.source)

            plugin.beans(getRepoBeanClosure(repoClass))
        }
    }

    static Closure getRepoBeanClosure(GrailsRepositoryClass repoClass, Object beanBuilder = null) {
        def lazyInit = repoClass.hasProperty("lazyInit") ? repoClass.getPropertyValue("lazyInit") : true

        def bClosure = {
            "${repoClass.propertyName}"(repoClass.getClazz()) { bean ->
                bean.autowire = true
                bean.lazyInit = lazyInit
            }
        }
        if (beanBuilder) bClosure.delegate = beanBuilder

        return bClosure
    }

    /**
     * Adds quickSearch fields to domains from config, if domain has such properties.
     * Supports paths for nested domains, for example "address.city", so if domain has
     * association address and it has property city it will be added
     *
     * @param config config bean
     * @param grailsApplication grails application context
     */
    static void addQuickSearchFields(List<String> fields, List<PersistentEntity> domains){
        domains.each { domainClass ->
            if (fields && !domainClass.getJavaClass().quickSearchFields) {
                domainClass.getJavaClass().quickSearchFields = fields.findAll {
                    GormMetaUtils.hasProperty(domainClass, it as String)
                }
            }
        }
    }

//    static void setFinalStatic(Field field, Object newValue) throws Exception {
//        field.setAccessible(true);
//
//        // remove final modifier from field
//        Field modifiersField = Field.class.getDeclaredField("modifiers");
//        modifiersField.setAccessible(true);
//        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
//
//        field.set(null, newValue);
//    }

}
