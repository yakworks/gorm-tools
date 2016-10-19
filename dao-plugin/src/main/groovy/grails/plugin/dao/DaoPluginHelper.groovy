package grails.plugin.dao
import grails.plugin.dao.DaoArtefactHandler
import grails.plugin.dao.GormDaoSupport
import grails.plugin.dao.GrailsDaoClass
import grails.transaction.Transactional
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.spring.TypeSpecifyableTransactionProxyFactoryBean
import org.codehaus.groovy.grails.orm.support.GroovyAwareNamedTransactionAttributeSource
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean

import java.lang.reflect.Method

class DaoPluginHelper {
	static final def log = Logger.getLogger(DaoPluginHelper)

	static def artefacts = [new DaoArtefactHandler()]

	static def doWithSpring = {
		gormDaoBeanNonTransactional(grails.plugin.dao.GormDaoSupport) { bean ->
			bean.scope = "prototype"
			//grailsApplication = ref('grailsApplication')
		}
		def props = new Properties()
		props."*" = "PROPAGATION_REQUIRED"
		gormDaoBean(TransactionProxyFactoryBean) { bean ->
			bean.scope = "prototype"
			bean.lazyInit = true
			target = ref('gormDaoBeanNonTransactional')
			proxyTargetClass = true
			transactionAttributeSource = new GroovyAwareNamedTransactionAttributeSource(transactionalAttributes:props)
			transactionManager = ref("transactionManager")
		}

		daoUtilBean(grails.plugin.dao.DaoUtil) //this is here just so the app ctx can get set on DaoUtils

		application.daoClasses.each {daoClass ->
			configureDaoBeans.delegate = delegate
			configureDaoBeans(daoClass,application)
		}

		//DaoUtils.ctx = application.mainContext
	}

	static def doWithDynamicMethods = { ctx ->
		//DaoUtils.ctx = ctx
		//force initialization of domain meta methods
		//forceInitGormMethods(application)

		modifyDomainsClasses(application,ctx)

	}


	static def onChange = { event ->
		if (!event.source || !event.ctx) {
			return
		}
		if (application.isArtefactOfType(DaoArtefactHandler.TYPE, event.source)) {

			def daoClass = application.addArtefact(DaoArtefactHandler.TYPE, event.source)

			def beans = beans {
				configureDaoBeans.delegate = delegate
				configureDaoBeans(daoClass)
			}

			def context = event.ctx
			context.registerBeanDefinition("${daoClass.fullName}DaoClass", beans.getBeanDefinition("${daoClass.fullName}DaoClass"))
			context.registerBeanDefinition("${daoClass.propertyName}", beans.getBeanDefinition("${daoClass.propertyName}"))
		}
		else if (application.isArtefactOfType(DomainClassArtefactHandler.TYPE, event.source)) {
			addNewPersistenceMethods(event.source,application,event.ctx)
		}
	}

	//Copied much of this from grails source ServicesGrailsPlugin
	static def configureDaoBeans = {GrailsDaoClass daoClass, application ->
		def scope = daoClass.getPropertyValue("scope")

		def lazyInit = daoClass.hasProperty("lazyInit") ? daoClass.getPropertyValue("lazyInit") : true

		"${daoClass.fullName}DaoClass"(MethodInvokingFactoryBean) { bean ->
			bean.lazyInit = lazyInit
			targetObject = ref("grailsApplication", true)
			targetMethod = "getArtefact"
			arguments = [DaoArtefactHandler.TYPE, daoClass.fullName]
		}

		if (shouldCreateTransactionalProxy(daoClass)) {
			def props = new Properties()
			String attributes = 'PROPAGATION_REQUIRED'
			String datasourceName = daoClass.datasource
			String suffix = datasourceName == GrailsDaoClass.DEFAULT_DATA_SOURCE ? '' : "_$datasourceName"
			if (application.config["dataSource$suffix"].readOnly) {
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
				transactionAttributeSource = new GroovyAwareNamedTransactionAttributeSource(transactionalAttributes:props)
				transactionManager = ref("transactionManager")
			}
		}
		else {
			"${daoClass.propertyName}"(daoClass.getClazz()) { bean ->
				bean.autowire =  true
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

	static def modifyDomainsClasses(GrailsApplication application, ApplicationContext ctx){
		for (GrailsDomainClass dc in application.domainClasses) {
			//forceInitGormMethods(dc.clazz)
			//MetaClass mc = dc.metaClass
			addNewPersistenceMethods(dc,application,ctx)
		}
	}

	static def addNewPersistenceMethods(GrailsDomainClass dc, GrailsApplication application, ApplicationContext ctx) {
		def metaClass = dc.metaClass
		//def origSaveArgs = dc.clazz.metaClass.getMetaMethod('save', Map)
		def dao = figureOutDao( dc, ctx)

		//TODO refactor this out as its copy pasted code for each method
		metaClass.persist = {Map args ->
			args['failOnError'] = true
			dao.save(delegate,args)
		}

		metaClass.persist = {->
			dao.save(delegate)
		}

		metaClass.remove = {->
			dao.delete(delegate)
		}

		metaClass.static.insert = { Map params ->
			dao.insert(params)
		}

		metaClass.static.update = { Map params ->
			dao.update(params)
		}

		metaClass.static.remove = { Map params ->
			dao.remove(params)
		}

		metaClass.static.getDao = { ->
			return dao
		}


	}

	static def figureOutDao(GrailsDomainClass dc, ApplicationContext ctx){
		def domainClass = dc.clazz
		def daoName = "${dc.propertyName}Dao"
		//def daoType = GrailsClassUtils.getStaticPropertyValue(domainClass, "daoType")
		def dao
		//println "$daoType and $daoName for $domainClass"
		//if(!daoType) {
		if(ctx.containsBean(daoName)){
			//println "found bean $daoName for $domainClass"
			dao = ctx.getBean(daoName)
		}else{
			//println "getInstance for $domainClass"
			//dao = GormDaoSupport.getInstance(domainClass)
			dao = ctx.getBean("gormDaoBean")
			dao.domainClass = domainClass
		}
		// }else{
		// 	if("transactional" == daoType){
		// 		//println "setting transactional bean  for $domainClass"
		// 		dao = ctx.getBean("gormDaoBean")
		// 		dao.domainClass = domainClass
		// 	}
		// 	else if(ctx.containsBean(daoType)){
		// 		dao = ctx.getBean(daoType)
		// 	}
		// }
		//if its still null then default it to a new instance
		if(!dao){
			//log.error "something went wrong trying to setup dao for ${dc.fullName} maybe this is wrong ${daoProps}"
			dao = GormDaoSupport.getInstance(dc.clazz)
		}

		return dao
	}

	//XXX this not even needed any more?
	static def forceInitGormMethods(domClass){
/*      //basically copied from the GormLabs code
        application.domainClasses*.clazz.each { domClass->*/
		try {
			domClass.thisIsATotallyBogusMethodPlacedHereJustToTriggerDynamicGORMMethods()
		} catch(MissingMethodException e) {
			return
		}
		//try on instance just in case if we get here
		try {
			domClass.newInstance().thisIsATotallyBogusMethodPlacedHereJustToTriggerGORMHydration()
		} catch(MissingMethodException e) {
			return
		}
		log.warn("Looks like we could not initialize $domClass via static methodMissing")
		//}
	}
}
