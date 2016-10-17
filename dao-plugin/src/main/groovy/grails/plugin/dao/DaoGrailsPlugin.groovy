package grails.plugin.dao
import grails.plugin.dao.DaoArtefactHandler
import grails.plugin.dao.DaoPluginHelper
import grails.plugin.dao.GormDaoSupport
import grails.plugin.dao.GrailsDaoClass
import org.grails.transaction.GroovyAwareNamedTransactionAttributeSource
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean

import java.lang.reflect.Method

class DaoGrailsPlugin extends grails.plugins.Plugin{
	def loadAfter = ['hibernate','datasources']

    def watchedResources = [
    	"file:./grails-app/dao/**/*Dao.groovy",
    	"file:./grails-app/services/**/*Dao.groovy",
    	"file:./grails-app/domain/**/*.groovy",
        "file:./plugins/*/grails-app/dao/**/*Dao.groovy",
        "file:./plugins/*/grails-app/services/**/*Dao.groovy"
    ]
	
	def artefacts = DaoPluginHelper.artefacts

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
			DaoPluginHelper.configureDaoBeans.delegate = delegate
			DaoPluginHelper.configureDaoBeans(daoClass,application)
		}

		//DaoUtils.ctx = application.mainContext
	}

    def doWithDynamicMethods = DaoPluginHelper.doWithDynamicMethods

    def onChange = DaoPluginHelper.onChange

	
}
