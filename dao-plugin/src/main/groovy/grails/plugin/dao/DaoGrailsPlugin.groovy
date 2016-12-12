package grails.plugin.dao

import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.spring.TypeSpecifyableTransactionProxyFactoryBean
import org.grails.transaction.GroovyAwareNamedTransactionAttributeSource
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean

class DaoGrailsPlugin extends grails.plugins.Plugin {
	def loadAfter = ['hibernate','datasources']

    def watchedResources = [
    	"file:./grails-app/dao/**/*Dao.groovy",
    	"file:./grails-app/services/**/*Dao.groovy",
    	"file:./grails-app/domain/**/*.groovy",
        "file:./plugins/*/grails-app/dao/**/*Dao.groovy",
        "file:./plugins/*/grails-app/services/**/*Dao.groovy"
    ]
	
	def artefacts = DaoPluginHelper.artefacts

    Closure doWithSpring() {{->
		Closure closure = DaoPluginHelper.doWithSpring
		closure.delegate = delegate
		closure.call()
	}}


	@Override
    void onChange(Map<String,Object> event) {
		DaoPluginHelper.onChange(event, grailsApplication)
	}


	
}
