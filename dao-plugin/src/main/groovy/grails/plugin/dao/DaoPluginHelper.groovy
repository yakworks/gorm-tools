package grails.plugin.dao

import gorm.tools.DbDialectService
import gorm.tools.idgen.BatchIdGenerator
import gorm.tools.idgen.IdGeneratorHolder
import gorm.tools.idgen.JdbcIdGenerator
import grails.core.ArtefactHandler
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.GrailsDomainClass
import grails.transaction.Transactional
import org.grails.spring.TypeSpecifyableTransactionProxyFactoryBean
import org.grails.transaction.GroovyAwareNamedTransactionAttributeSource
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean

import java.lang.reflect.Method

class DaoPluginHelper {
	static List<ArtefactHandler> artefacts = [new DaoArtefactHandler()]

	static Closure doWithSpring = {
		jdbcTemplate(org.springframework.jdbc.core.JdbcTemplate, ref("dataSource"))

		jdbcIdGenerator(JdbcIdGenerator){
			jdbcTemplate = ref("jdbcTemplate")
			table = "NewObjectId"
			keyColumn="KeyName"
			idColumn="NextId"
		}
		idGenerator(BatchIdGenerator){
			generator = ref("jdbcIdGenerator")
		}
		idGeneratorHolder(IdGeneratorHolder){
			idGenerator = ref("idGenerator")
		}

		gormDaoBeanNonTransactional(grails.plugin.dao.GormDaoSupport) { bean ->
			bean.scope = "prototype"
			//grailsApplication = ref('grailsApplication')
		}
		Properties props = new Properties()
		props."*" = "PROPAGATION_REQUIRED"
		gormDaoBean(TransactionProxyFactoryBean) { bean ->
			bean.scope = "prototype"
			bean.lazyInit = true
			target = ref('gormDaoBeanNonTransactional')
			proxyTargetClass = true
			transactionAttributeSource = new GroovyAwareNamedTransactionAttributeSource(transactionalAttributes: props)
			transactionManager = ref("transactionManager")
		}

		daoUtilBean(grails.plugin.dao.DaoUtil) //this is here just so the app ctx can get set on DaoUtils

		application.daoClasses.each { daoClass ->

			Closure closure = configureDaoBeans
			closure.delegate = delegate
			closure.call(daoClass, grailsApplication)

		}
		DbDialectService.dialectName = application.config.hibernate.dialect

		//DaoUtils.ctx = application.mainContext*/
	}

	static void onChange(event, GrailsApplication grailsApplication) {
		if (!event.source || !event.ctx) {
			return
		}
		if (grailsApplication.isArtefactOfType(DaoArtefactHandler.TYPE, event.source)) {

			GrailsClass daoClass = grailsApplication.addArtefact(DaoArtefactHandler.TYPE, event.source)

			def beans = beans {
				Closure closure = configureDaoBeans
				closure.delegate = delegate
				closure.call(daoClass, grailsApplication)
			}

			def context = event.ctx
			context.registerBeanDefinition("${daoClass.fullName}DaoClass", beans.getBeanDefinition("${daoClass.fullName}DaoClass"))
			context.registerBeanDefinition("${daoClass.propertyName}", beans.getBeanDefinition("${daoClass.propertyName}"))
		}
	}

	//Copied much of this from grails source ServicesGrailsPlugin
	static Closure configureDaoBeans = { GrailsDaoClass daoClass, GrailsApplication grailsApplication ->
		def scope = daoClass.getPropertyValue("scope")

		def lazyInit = daoClass.hasProperty("lazyInit") ? daoClass.getPropertyValue("lazyInit") : true

		"${daoClass.fullName}DaoClass"(MethodInvokingFactoryBean) { bean ->
			bean.lazyInit = lazyInit
			targetObject = grailsApplication
			targetMethod = "getArtefact"
			arguments = [DaoArtefactHandler.TYPE, daoClass.fullName]
		}

		//FIXME can't we get rid of this now? GRails doesn't do it in the services right?
		//ALSO see here for how they do it
		// https://github.com/grails/grails-data-mapping/blob/1b14ecf85b221fc78d363001ea960728d7902b45/grails-datastore-gorm-plugin-support/src/main/groovy/org/grails/datastore/gorm/plugin/support/SpringConfigurer.groovy#L102-L102
		//Also see http://docs.grails.org/latest/guide/single.html#upgrading under "Spring Proxies for Services No Longer Supported"
		//What does that mean for this here?
		if (shouldCreateTransactionalProxy(daoClass)) {
			Properties props = new Properties()
			String attributes = 'PROPAGATION_REQUIRED'
			String datasourceName = daoClass.datasource
			String suffix = datasourceName == GrailsDaoClass.DEFAULT_DATA_SOURCE ? '' : "_$datasourceName"
			if (grailsApplication.config["dataSource$suffix"].readOnly) {
				attributes += ',readOnly'
			}
			props."*" = attributes

			"${daoClass.propertyName}"(TypeSpecifyableTransactionProxyFactoryBean, daoClass.clazz) { bean ->
				if (scope) bean.scope = scope
				bean.lazyInit = lazyInit
				target = { innerBean ->
					innerBean.lazyInit = true
					innerBean.factoryBean = "${daoClass.fullName}DaoClass"
					innerBean.factoryMethod = "newInstance"
					innerBean.autowire = "byName"
					if (scope) innerBean.scope = scope
				}
				proxyTargetClass = true
				transactionAttributeSource = new GroovyAwareNamedTransactionAttributeSource(transactionalAttributes: props)
				transactionManager = ref("transactionManager")
			}
		} else {
			"${daoClass.propertyName}"(daoClass.getClazz()) { bean ->
				bean.autowire = true
				bean.lazyInit = lazyInit
				if (scope) bean.scope = scope
			}
		}
	}

	static boolean shouldCreateTransactionalProxy(GrailsDaoClass daoClass) {
		Class javaClass = daoClass.clazz

		try {
			daoClass.transactional &&
					!AnnotationUtils.findAnnotation(javaClass, grails.transaction.Transactional) &&
					!AnnotationUtils.findAnnotation(javaClass, Transactional) &&
					!javaClass.methods.any { Method m -> AnnotationUtils.findAnnotation(m, Transactional) != null ||
							AnnotationUtils.findAnnotation(m, grails.transaction.Transactional) != null}
		}
		catch (e) {
			return false
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
