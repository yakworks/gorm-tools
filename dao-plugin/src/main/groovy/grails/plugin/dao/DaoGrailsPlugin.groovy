package grails.plugin.dao

import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.spring.TypeSpecifyableTransactionProxyFactoryBean
import org.grails.transaction.GroovyAwareNamedTransactionAttributeSource
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean

class DaoGrailsPlugin extends grails.plugins.Plugin {
	def loadAfter = ['hibernate','datasources']

    def watchedResources = [
    	"file:./grails-app/daotesting/**/*Dao.groovy",
    	"file:./grails-app/services/**/*Dao.groovy",
    	"file:./grails-app/domain/**/*.groovy",
        "file:./plugins/*/grails-app/daotesting/**/*Dao.groovy",
        "file:./plugins/*/grails-app/services/**/*Dao.groovy"
    ]
	
	def artefacts = DaoPluginHelper.artefacts

    Closure doWithSpring() {{->
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

			def scope = daoClass.getPropertyValue("scope")

			def lazyInit = daoClass.hasProperty("lazyInit") ? daoClass.getPropertyValue("lazyInit") : true

			"${daoClass.fullName}DaoClass"(MethodInvokingFactoryBean) { bean ->
				bean.lazyInit = lazyInit
				targetObject = application
				targetMethod = "getArtefact"
				arguments = [DaoArtefactHandler.TYPE, daoClass.fullName]
			}

			if (shouldCreateTransactionalProxy(daoClass)) {
				props = new Properties()
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
					transactionAttributeSource = new GroovyAwareNamedTransactionAttributeSource(transactionalAttributes:props)
					transactionManager = ref("transactionManager")
				}
			} else {
				"${daoClass.propertyName}"(daoClass.getClazz()) { bean ->
					bean.autowire =  true
					bean.lazyInit = lazyInit
					if (scope) bean.scope = scope
				}
			}

		}

		//DaoUtils.ctx = application.mainContext*/
	}}

	@Override
	void doWithDynamicMethods(){
		DaoPluginHelper.doWithDynamicMethods(grailsApplication, getApplicationContext())
	}
    //def doWithDynamicMethods = DaoPluginHelper.doWithDynamicMethods

	@Override
    void onChange(Map<String,Object> event) {
		if (!event.source || !event.ctx) {
			return
		}
		if (grailsApplication.isArtefactOfType(DaoArtefactHandler.TYPE, event.source)) {

			def daoClass = grailsApplication.addArtefact(DaoArtefactHandler.TYPE, event.source)

			def beans = beans {
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
						transactionAttributeSource = new GroovyAwareNamedTransactionAttributeSource(transactionalAttributes:props)
						transactionManager = ref("transactionManager")
					}
				} else {
					"${daoClass.propertyName}"(daoClass.getClazz()) { bean ->
						bean.autowire =  true
						bean.lazyInit = lazyInit
						if (scope) bean.scope = scope
					}
				}
			}

			def context = event.ctx
			context.registerBeanDefinition("${daoClass.fullName}DaoClass", beans.getBeanDefinition("${daoClass.fullName}DaoClass"))
			context.registerBeanDefinition("${daoClass.propertyName}", beans.getBeanDefinition("${daoClass.propertyName}"))
		}
		else if (grailsApplication.isArtefactOfType(DomainClassArtefactHandler.TYPE, event.source)) {
			addNewPersistenceMethods(event.source,event.ctx)
		}
	}


	
}
