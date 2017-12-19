package grails.plugin.gormtools

import gorm.tools.DbDialectService
import gorm.tools.GormMetaUtils
import gorm.tools.async.GparsBatchSupport
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.idgen.BatchIdGenerator
import gorm.tools.idgen.IdGeneratorHolder
import gorm.tools.idgen.JdbcIdGenerator
import gorm.tools.mango.MangoQuery
import grails.config.Config
import grails.core.ArtefactHandler
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.Plugin
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.jdbc.core.JdbcTemplate

@SuppressWarnings(['NoDef'])
class GormToolsPluginHelper {
    static List<ArtefactHandler> artefacts = [new RepositoryArtefactHandler()]

    static Closure doWithSpring = {
        jdbcTemplate(JdbcTemplate, ref("dataSource"))

        entityMapBinder(EntityMapBinder)

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
        repoUtilBean(RepoUtil) //this is here just so the app ctx can get picked up and set on the static

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
            def hasRepo = repoClasses.find { it.propertyName == repoName }
            if (!hasRepo) {
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

}
