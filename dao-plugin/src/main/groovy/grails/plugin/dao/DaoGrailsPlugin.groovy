package grails.plugin.dao

import grails.core.ArtefactHandler

@SuppressWarnings(['NoDef'])
class DaoGrailsPlugin extends grails.plugins.Plugin {
	def loadAfter = ['hibernate','datasources']

    def watchedResources = [
    	"file:./grails-app/dao/**/*Dao.groovy",
    	"file:./grails-app/services/**/*Dao.groovy",
    	"file:./grails-app/domain/**/*.groovy",
        "file:./plugins/*/grails-app/dao/**/*Dao.groovy",
        "file:./plugins/*/grails-app/services/**/*Dao.groovy"
    ]

	List<ArtefactHandler> artefacts = DaoPluginHelper.artefacts

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
