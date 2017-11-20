package grails.plugin.dao

import gorm.tools.DbDialectService
import gorm.tools.idgen.BatchIdGenerator
import gorm.tools.idgen.IdGeneratorHolder
import gorm.tools.idgen.JdbcIdGenerator
import grails.core.ArtefactHandler
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.GrailsDomainClass
import grails.plugins.Plugin
import org.springframework.jdbc.core.JdbcTemplate

@SuppressWarnings(['NoDef'])
class DaoPluginHelper {
	static List<ArtefactHandler> artefacts = [new DaoArtefactHandler()]

	static Closure doWithSpring = {
		jdbcTemplate(JdbcTemplate, ref("dataSource"))

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

        gormDaoBean(grails.plugin.dao.GormDaoSupport) { bean ->
			bean.scope = "prototype"
			//grailsApplication = ref('grailsApplication')
		}

		daoUtilBean(grails.plugin.dao.DaoUtil) //this is here just so the app ctx can get set on DaoUtils

        def daoClasses = application.daoClasses
		daoClasses.each { daoClass ->

			Closure closure = configureDaoBeans
			closure.delegate = delegate
			closure.call(daoClass, grailsApplication)

		}
		DbDialectService.dialectName = application.config.hibernate.dialect

        application.domainClasses.each { GrailsDomainClass dc ->
            Class domainClass = dc.clazz
            String daoName = "${dc.propertyName}Dao"
            def hasDao = daoClasses.find { it.propertyName ==  daoName}
            if(!hasDao){
                println "${daoName}"
                "${daoName}"(grails.plugin.dao.GormDaoSupport, domainClass) { bean ->
                    bean.autowire = true
                    bean.lazyInit = true
                }
            }
        }
		//DaoUtils.ctx = application.mainContext*/
	}

	static void onChange(event, GrailsApplication grailsApplication, Plugin plugin) {
		if (!event.source || !event.ctx) {
			return
		}
		if (grailsApplication.isArtefactOfType(DaoArtefactHandler.TYPE, event.source)) {

			GrailsClass daoClass = grailsApplication.addArtefact(DaoArtefactHandler.TYPE, event.source)

			plugin.beans {
				Closure closure = configureDaoBeans
				closure.delegate = delegate
				return closure.call(daoClass, grailsApplication)
			}
		}
	}

	//Copied much of this from grails source ServicesGrailsPlugin
	static Closure configureDaoBeans = { GrailsDaoClass daoClass, GrailsApplication grailsApplication ->
		def scope = daoClass.getPropertyValue("scope")

		def lazyInit = daoClass.hasProperty("lazyInit") ? daoClass.getPropertyValue("lazyInit") : true
//
//		"${daoClass.fullName}DaoClass"(MethodInvokingFactoryBean) { bean ->
//			bean.lazyInit = lazyInit
//			targetObject = grailsApplication
//			targetMethod = "getArtefact"
//			arguments = [DaoArtefactHandler.TYPE, daoClass.fullName]
//		}

        "${daoClass.propertyName}"(daoClass.getClazz()) { bean ->
            bean.autowire = true
            bean.lazyInit = lazyInit
            if (scope) bean.scope = scope
        }
	}

	static def figureOutDao(GrailsDomainClass dc, ctx) {
		def domainClass = dc.clazz
		String daoName = "${dc.propertyName}Dao"
		//def daoType = GrailsClassUtils.getStaticPropertyValue(domainClass, "daoType")
		def dao
		//println "$daoType and $daoName for $domainClass"
		if (ctx.containsBean(daoName)) {
			//println "found bean $daoName for $domainClass"
			dao = ctx.getBean(daoName)
		} else {
			//println "getInstance for $domainClass"
			dao = ctx.getBean("gormDaoBean")
			dao.domainClass = domainClass
		}

		return dao
	}

}
