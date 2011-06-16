package grails.plugin.dao

import org.springframework.validation.Errors
import grails.test.*
import testing.*

class GormDaoSupportTests extends BasicTestsForDao {

	def daoFactory
	
	protected void setUp() {
		super.setUp()
		dao = daoFactory.getDao(Jumper) //new GormDaoSupport(Jumper)
		assert dao.domainClass == Jumper
		//pop an item in just to have for things below
	}

	protected void tearDown() {
		super.tearDown()
	}


}

