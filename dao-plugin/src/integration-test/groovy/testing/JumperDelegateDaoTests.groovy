package testing

import org.springframework.validation.Errors
import grails.test.*
import org.junit.*
import grails.plugin.dao.*

class JumperDelegateDaoTests //FIXME extends BasicTestsForDao 
{

	static transactional = false
	
	def jumperDelegateDao
	
	// protected void setUp() {
	// 	//super.setUp()
	// 	//dao = jumperDelegateDao
	// 	//assert dao.domainClass == Jumper
	// }
	
	@Test
	void testNonTranDao(){
		println "testSave"
		def dom = new Jumper(name:"testSave")
		assert dom.persist()
	}


}

