package daoapp
import grails.plugin.dao.GormDaoSupport

class OrgDao extends GormDaoSupport{
	Class domainClass = Org
	
	Map insert(params){
		def madeNameDefault
		if(!params.name){
			params.name = "default"
		}
		if (params.name){
			params.name += " from Dao"
		}
		super.insert(params)
	}
}

