package grails.plugin.dao

import grails.util.GrailsNameUtils
import grails.util.Holders
import org.grails.datastore.gorm.GormEntity

trait DaoDomainTrait {
	static def getDao() {
		def grailsApplication = Holders.grailsApplication
		def domainName = this.name
		Class domainClass = grailsApplication.getDomainClass(domainName).clazz
		def daoName = "${GrailsNameUtils.getPropertyName(domainName)}Dao"
		def dao
		if(grailsApplication.mainContext.containsBean(daoName)){
			dao = grailsApplication.mainContext.getBean(daoName)
		}else{
			dao = grailsApplication.mainContext.getBean("gormDaoBean")
			dao.domainClass = domainClass
		}
		if(!dao){
			dao = GormDaoSupport.getInstance(domainClass)
		}
		return dao
	}

	def persist(Map args){
		args['failOnError'] = true
		dao.save(this, args)
	}

	def persist(){
		dao.save(this)
	}

	def remove(){
		dao.delete(this)
	}

	static def insertAndSave(Map params){
		dao.insert(params)
	}

	static def update(Map params){
		dao.update(params)
	}

	static def remove(Map params){
		dao.remove(params)
	}
}
