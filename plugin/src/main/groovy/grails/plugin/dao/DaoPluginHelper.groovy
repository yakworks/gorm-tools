package grails.plugin.dao

import gorm.tools.DbDialectService
import gorm.tools.async.GparsBatchService
import gorm.tools.dao.DaoUtil
import gorm.tools.dao.DefaultGormDao
import gorm.tools.dao.events.DaoEventPublisher
import gorm.tools.databinding.FastBinder
import gorm.tools.idgen.BatchIdGenerator
import gorm.tools.idgen.IdGeneratorHolder
import gorm.tools.idgen.JdbcIdGenerator
import grails.core.ArtefactHandler
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.Plugin
import grails.util.GrailsNameUtils
import org.springframework.jdbc.core.JdbcTemplate

@SuppressWarnings(['NoDef'])
class DaoPluginHelper {
    static List<ArtefactHandler> artefacts = [new DaoArtefactHandler()]

    static Closure doWithSpring = {
        jdbcTemplate(JdbcTemplate, ref("dataSource"))

        fastBinder(FastBinder)

        jdbcIdGenerator(JdbcIdGenerator){
            jdbcTemplate = ref("jdbcTemplate")
            table = "NewObjectId"
            keyColumn="KeyName"
            idColumn="NextId"
        }
        idGenerator(BatchIdGenerator){
            generator = ref("jdbcIdGenerator")
        }
        //here to set the static in the holder for use in SpringIdGenerator
        idGeneratorHolder(IdGeneratorHolder){
            idGenerator = ref("idGenerator")
        }

        daoEventInvoker(DaoEventPublisher)
        daoUtilBean(DaoUtil) //this is here just so the app ctx can get picked up and set on DaoUtils

        gparsBatchService(GparsBatchService)

        DbDialectService.dialectName = application.config.hibernate.dialect

        def daoClasses = application.daoClasses
        daoClasses.each { daoClass ->
            getDaoBeanClosure(daoClass, delegate).call()
        }

        //make sure each domain has a dao, if not set up a DefaultGormDao for it.
        Class[] domainClasses = application.domainClasses*.clazz
        domainClasses.each { Class domainClass ->
            String daoName = "${GrailsNameUtils.getPropertyName(domainClass.name)}Dao"
            def hasDao = daoClasses.find { it.propertyName ==  daoName}
            if(!hasDao){
                //println "${daoName}"
                "${daoName}"(DefaultGormDao, domainClass) { bean ->
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
        if (grailsApplication.isArtefactOfType(DaoArtefactHandler.TYPE, event.source)) {

            GrailsClass daoClass = grailsApplication.addArtefact(DaoArtefactHandler.TYPE, event.source)

            plugin.beans(getDaoBeanClosure(daoClass))
        }
    }

    static Closure getDaoBeanClosure(GrailsDaoClass daoClass, beanBuilder = null){
        def lazyInit = daoClass.hasProperty("lazyInit") ? daoClass.getPropertyValue("lazyInit") : true

        def bClosure = {
            "${daoClass.propertyName}"(daoClass.getClazz()) { bean ->
                bean.autowire = true
                bean.lazyInit = lazyInit
            }
        }
        if (beanBuilder) bClosure.delegate = beanBuilder

        return bClosure
    }

}
