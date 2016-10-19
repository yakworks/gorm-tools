import grails.plugin.dao.DaoPluginHelper
import grails.plugin.dao.GormDaoSupport
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.orm.support.GroovyAwareNamedTransactionAttributeSource
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean

class DaoGrailsPlugin {
	static final def log = Logger.getLogger(DaoGrailsPlugin)
	
    // the plugin version
    def version = "1.0.3"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.5.0 > *"
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
	def license = "APACHE"
	def organization = [ name: "9ci", url: "http://www.9ci.com/" ]
	def developers = [ [ name: "Joshua Burnet", email: "joshua@greenbill.com" ]]
	def issueManagement = [ system: "github", url: "https://github.com/9ci/grails-dao/issues" ]
	def scm = [ url: "https://github.com/9ci/grails-dao" ]
	def documentation = "https://github.com/9ci/grails-dao"
	
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
