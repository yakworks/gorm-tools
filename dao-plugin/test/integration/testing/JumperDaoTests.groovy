package testing

import org.springframework.validation.Errors
import grails.test.*
import grails.plugin.dao.*

class JumperDaoTests extends BasicTestsForDao {

	static transactional = false
	
	def jumperDao
	
	protected void setUp() {
		super.setUp()
		dao = jumperDao
		assert dao.domainClass == Jumper
	}
	
	void testNonTranDao(){
		println "testSave"
		def dom = new Jumper(name:"testSave")
		assert dom.persist()
	}


}

