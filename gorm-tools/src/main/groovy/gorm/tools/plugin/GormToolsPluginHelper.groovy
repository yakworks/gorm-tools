/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.plugin

import groovy.transform.CompileDynamic

import org.grails.core.artefact.DomainClassArtefactHandler
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.async.GparsAsyncSupport
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
import gorm.tools.support.ErrorMessageService
import gorm.tools.support.MsgService
import gorm.tools.transaction.TrxService
import grails.core.ArtefactHandler
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.Plugin

@SuppressWarnings(['UnnecessarySelfAssignment', 'Println'])
@CompileDynamic
class GormToolsPluginHelper {
    static List<ArtefactHandler> artefacts = [new RepositoryArtefactHandler()]

    static Closure doWithSpring = {
        println "starting gorm-tools config"
        msgService(MsgService)
        errorMessageService(ErrorMessageService){ bean -> bean.lazyInit = true}

        jdbcTemplate(JdbcTemplate, ref("dataSource")){ bean -> bean.lazyInit = true}

        jdbcIdGenerator(JdbcIdGenerator) { bean ->
            bean.lazyInit = true
            jdbcTemplate = jdbcTemplate
            table = "NewObjectId"
            keyColumn = "KeyName"
            idColumn = "NextId"
        }

        idGenerator(PooledIdGenerator, jdbcIdGenerator){ bean -> bean.lazyInit = true}

        mangoQuery(DefaultMangoQuery){ bean -> bean.lazyInit = true}
        mangoBuilder(MangoBuilder){ bean -> bean.lazyInit = true}

        entityMapBinder(EntityMapBinder, ref('grailsApplication')){ bean -> bean.lazyInit = true}
        entityMapService(EntityMapService){ bean -> bean.lazyInit = true}

        repoEventPublisher(RepoEventPublisher){ bean -> bean.lazyInit = true}

        repoExceptionSupport(RepoExceptionSupport){ bean -> bean.lazyInit = true}

        asyncSupport(GparsAsyncSupport){ bean -> bean.lazyInit = true}

        DbDialectService.dialectName = application.config.hibernate.dialect

        dbDialectService(DbDialectService) { bean ->
            bean.lazyInit = true
            jdbcTemplate = ref('jdbcTemplate')
        }

        trxService(TrxService){ bean -> bean.lazyInit = true}

        def repoClasses = application.repositoryClasses
        repoClasses.each { repoClass ->
            getRepoBeanClosure(repoClass, delegate).call()
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

        println "finished gorm-tools config"
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

}
