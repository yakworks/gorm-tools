package daoapp

import grails.plugin.dao.GormDaoSupport

class OrgDelegateDao extends GormDaoSupport{
	
	Class domainClass = Org
	
	Map insert(params){
		def madeNameDefault
		if(!params.name){
			params.name = "default"
		}
		super.insert(params)
	}
}

