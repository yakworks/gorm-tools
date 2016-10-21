package testing1
import grails.plugin.dao.*
import javax.annotation.PostConstruct

//FIXME can't get delegate working
//this is here to show that these will get picked up in the services directory too
class JumperDelegateDao extends  GormDaoSupport{
	//static transactional = false
	
	Class domainClass = Jumper
	def grailsApplication

	@PostConstruct
	def init() {
		println "WTF"
		def dao = grailsApplication.mainContext.getBean("gormDaoBean")
		dao.domainClass = domainClass
	}
}

