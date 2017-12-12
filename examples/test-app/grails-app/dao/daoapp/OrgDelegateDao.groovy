package daoapp

import gorm.tools.dao.DefaultGormDao
import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileDynamic

@GrailsCompileStatic
class OrgDelegateDao extends DefaultGormDao<Org> {

	@Override
	@CompileDynamic
    Org create(Map params){
		if(!params.name){
			params.name = "default"
		}
		super.create(params)
	}
}

