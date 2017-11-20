package daoapp

import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic

@Transactional
@GrailsCompileStatic
class OrgDao extends GormDaoSupport<Org> {
	//Class domainClass = Org

	@CompileDynamic
	@Override
	Map insert(Map params){
		def madeNameDefault
		if(!params.name){
			params.name = "default"
		}
		if (params.name){
			params.name += " from Dao"
		}
		super.doInsert(params)
	}
}

