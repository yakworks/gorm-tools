package grails.plugin.dao

import org.springframework.validation.Errors
import grails.test.*
import testing.*

class GormDaoSupportTests extends BasicTestsForDao {

	def grailsApplication
	
	void setUp() {
		super.setUp()
		dao = grailsApplication.mainContext.gormDaoBean//new GormDaoSupport(Jumper)
		dao.domainClass = Jumper
		assert dao.domainClass == Jumper
		//pop an item in just to have for things below
	}


}

