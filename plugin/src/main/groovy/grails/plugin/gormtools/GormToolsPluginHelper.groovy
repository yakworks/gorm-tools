package grails.plugin.gormtools

import gorm.tools.DbDialectService
import gorm.tools.async.GparsBatchSupport
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.databinding.GormMapBinder
import gorm.tools.idgen.BatchIdGenerator
import gorm.tools.idgen.IdGeneratorHolder
import gorm.tools.idgen.JdbcIdGenerator
import gorm.tools.mango.MangoQuery
import grails.core.ArtefactHandler
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.Plugin
import org.springframework.jdbc.core.JdbcTemplate

@SuppressWarnings(['NoDef'])
class GormToolsPluginHelper {
    static List<ArtefactHandler> artefacts = [new RepositoryArtefactHandler()]

    static Closure doWithSpring = {
        jdbcTemplate(JdbcTemplate, ref("dataSource"))

        gormMapBinder(GormMapBinder)

        jdbcIdGenerator(JdbcIdGenerator) {
            jdbcTemplate = ref("jdbcTemplate")
            table = "NewObjectId"
            keyColumn = "KeyName"
            idColumn = "NextId"
        }
        idGenerator(BatchIdGenerator) {
            generator = ref("jdbcIdGenerator")
        }
        //here to set the static in the holder for use in SpringIdGenerator
        idGeneratorHolder(IdGeneratorHolder) {
            idGenerator = ref("idGenerator")
        }

        mangoQuery(MangoQuery)
        repoEventPublisher(RepoEventPublisher)
        repoUtilBean(RepoUtil) //this is here just so the app ctx can get picked up and set on DaoUtils

        asyncBatchSupport(GparsBatchSupport)

        DbDialectService.dialectName = application.config.hibernate.dialect

        def repoClasses = application.repositoryClasses
        repoClasses.each { repoClass ->
            getRepoBeanClosure(repoClass, delegate).call()
        }

        //make sure each domain has a repository, if not set up a DefaultGormRepo for it.
        Class[] domainClasses = application.domainClasses*.clazz
        domainClasses.each { Class domainClass ->
            String repoName = RepoUtil.getRepoBeanName(domainClass)
            def hasDao = repoClasses.find { it.propertyName == repoName }
            if (!hasDao) {
                //println "${repoName}"
                "${repoName}"(DefaultGormRepo, domainClass) { bean ->
                    bean.autowire = true
                    bean.lazyInit = true
                }
            }
        }
    }

    static void onChange(event, GrailsApplication grailsApplication, Plugin plugin) {
        if (!event.source || !event.ctx) {
            return
        }
        if (grailsApplication.isArtefactOfType(RepositoryArtefactHandler.TYPE, event.source)) {

            GrailsClass repoClass = grailsApplication.addArtefact(RepositoryArtefactHandler.TYPE, event.source)

            plugin.beans(getRepoBeanClosure(repoClass))
        }
    }

    static Closure getRepoBeanClosure(GrailsRepositoryClass repoClass, beanBuilder = null) {
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
