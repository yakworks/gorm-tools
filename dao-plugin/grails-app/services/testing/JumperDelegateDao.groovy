package testing
import grails.plugin.dao.*
import org.springframework.beans.factory.InitializingBean
import javax.annotation.PostConstruct

//FIXME can't get delegate working
//this is here to show that these will get picked up in the services directory too
class JumperDelegateDao { 
	//static transactional = false
	
	def domainClass = Jumper
	def grailsApplication

	//@Delegate 
	GormDao dao //= GormDao.getBean(domainClass) 

/*	public void afterPropertiesSet() {
	        // do some initialization work
	}*/
	@PostConstruct
	def init() {
		println "WTF"
		def dao = grailsApplication.mainContext.getBean("gormDaoBean")
		dao.domainClass = domainClass
	}
}

