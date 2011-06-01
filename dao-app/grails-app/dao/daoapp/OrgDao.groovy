package testing
import grails.plugin.dao.*

class OrgDao extends GormDao{ 
	def domainClass = Org
	
	def insert(params){
		def madeNameDefault
		if(!params.name){
			params.name = "default"
		}
		super.insert(params)
	}
}

