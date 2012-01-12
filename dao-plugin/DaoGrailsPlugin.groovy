import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;
import grails.validation.ValidationException

import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.spring.TypeSpecifyableTransactionProxyFactoryBean
import org.codehaus.groovy.grails.commons.GrailsServiceClass
import org.codehaus.groovy.grails.commons.ServiceArtefactHandler
import org.codehaus.groovy.grails.orm.support.GroovyAwareNamedTransactionAttributeSource

import org.springframework.context.ApplicationContext
import org.apache.log4j.Logger

import grails.util.GrailsUtil

import java.lang.reflect.Method

import grails.plugin.dao.*

class DaoGrailsPlugin {
	static final def log = Logger.getLogger(DaoGrailsPlugin)
	
    // the plugin version
    def version = "0.4.2"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.4 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
		"grails-app/views/error.gsp",
		"grails-app/**/*",
		"web-app/**/*"
    ]

    // TODO Fill in these fields
    def author = "Joshua Burnett"
    def authorEmail = ""
    def title = "Dao plugin"
    def description = '''\\
Enables a grails-app/dao directory to setup beans
see https://github.com/basejump/grails-dao
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/dao"
	
	def loadAfter = ['hibernate','datasources']

    def watchedResources = ["file:./grails-app/dao/**/*Dao.groovy","file:./grails-app/services/**/*Dao.groovy",
                            "file:./plugins/*/grails-app/dao/**/*Dao.groovy","file:./plugins/*/grails-app/services/**/*Dao.groovy"]
	
	def artefacts = [new DaoArtefactHandler()]

    def doWithSpring = {
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
            configureDaoBeans(daoClass)
        }

		//DaoUtils.ctx = application.mainContext
    }

    def doWithDynamicMethods = { ctx ->
		//DaoUtils.ctx = ctx
		//force initialization of domain meta methods
		forceInitGormMethods(application)
		
		modifyDomainsClasses(application,ctx)

    }


    def onChange = { event ->
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
    }

    def configureDaoBeans = {GrailsDaoClass daoClass ->
		def scope = daoClass.getPropertyValue("scope")
		
		"${daoClass.fullName}DaoClass"(MethodInvokingFactoryBean) { bean ->
            bean.lazyInit = true
            targetObject = ref("grailsApplication", true)
            targetMethod = "getArtefact"
            arguments = [DaoArtefactHandler.TYPE, daoClass.fullName]
        }
		if (shouldCreateTransactionalProxy(daoClass)) {
            def props = new Properties()
            props."*" = "PROPAGATION_REQUIRED"
            "${daoClass.propertyName}"(TypeSpecifyableTransactionProxyFactoryBean, daoClass.clazz) { bean ->
                if (scope) bean.scope = scope
                bean.lazyInit = true
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
                bean.lazyInit = true
                if (scope) bean.scope = scope
            }
        }
    }

    def shouldCreateTransactionalProxy(GrailsDaoClass daoClass) {
        Class javaClass = daoClass.clazz

        try {
            daoClass.transactional &&
              !AnnotationUtils.findAnnotation(javaClass, Transactional) &&
                 !javaClass.methods.any { Method m -> AnnotationUtils.findAnnotation(m, Transactional)!=null }
        }
        catch (e) {
            return false
        }
    }

	def modifyDomainsClasses(GrailsApplication application, ApplicationContext ctx){
		for (GrailsDomainClass dc in application.domainClasses) {
            MetaClass mc = dc.metaClass
			addNewPersistenceMethods(dc,application,ctx)
		}
	}

	def addNewPersistenceMethods(GrailsDomainClass dc, GrailsApplication application, ApplicationContext ctx) {
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
	
	def figureOutDao(GrailsDomainClass dc, ApplicationContext ctx){
		def domainClass = dc.clazz
		def daoName = "${dc.propertyName}Dao"
		def daoType = GrailsClassUtils.getStaticPropertyValue(domainClass, "daoType")
		def dao
		//println "$daoType and $daoName for $domainClass"
		if(!daoType) {
			if(ctx.containsBean(daoName)){
				//println "found bean $daoName for $domainClass"
				dao = ctx.getBean(daoName)
			}else{
				//println "getInstance for $domainClass"
				dao = GormDaoSupport.getInstance(domainClass)
			}
		}else{
			if("transactional" == daoType){
				//println "setting transactional bean  for $domainClass"
				dao = ctx.getBean("gormDaoBean")
				dao.domainClass = domainClass
			}
			else if(ctx.containsBean(daoType)){
				dao = ctx.getBean(daoType)
			}
		}
		//if its still null then default it to a new instance
		if(!dao){
			log.error "something went wrong trying to setup dao for ${dc.fullName} maybe this is wrong ${daoProps}"
			dao = GormDaoSupport.getInstance(dc.clazz)
		} 
		
		return dao
	}
	
	def forceInitGormMethods(application){
		//basically copied from the GormLabs code
		application.domainClasses*.clazz.each {
			int methodPreCount = it.metaClass.methods.size()
			boolean sawError = false
			try {
				it.thisIsATotallyBogusMethodPlacedHereJustToTriggerGORMHydration()
			} catch(MissingMethodException e) {
				sawError = true
			}
			if(!sawError) log.warn("Looks like we could not initialize $it via static methodMissing")

			sawError = false
			try {
				it.newInstance().thisIsATotallyBogusMethodPlacedHereJustToTriggerGORMHydration()
		  	} catch(MissingMethodException e) {
				sawError = true
			}
			if(!sawError) log.warn("Looks like we could not initialize $it via instance methodMissing")

			int methodPostCount = it.metaClass.methods.size()
			if(!(methodPreCount < methodPostCount)) log.warn("Doesn't look like $it was hydrated")
		}
	}
	
}
