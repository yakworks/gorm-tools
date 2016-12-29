package daoapp

import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport
import groovy.transform.CompileDynamic

@GrailsCompileStatic
class OrgDelegateDao extends GormDaoSupport<Org>{
	
	Class domainClass = Org

	@Override
	@CompileDynamic
	Map insert(Map params){
		def madeNameDefault
		if(!params.name){
			params.name = "default"
		}
		super.insert(params)
	}
}

