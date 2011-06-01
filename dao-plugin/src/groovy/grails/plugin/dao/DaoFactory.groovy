package grails.plugin.dao

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

//Used as a bean to get new Dao setup for a Gorm domain class
class DaoFactory {
	
	def grailsApplication
	
	public DaoFactory(){}

	//gets a dao that is setup for the domain class
	//done this way so you can get the prototype bean easily. 
	//You want a protoype bean so that it all setup with the transactional wrapping
	def getDao(Class domainClass){
		def dao = grailsApplication.mainContext.getBean("gormDaoBean")
		dao.domainClass = domainClass
		return dao
	}
}

