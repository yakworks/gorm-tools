package testing

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.validation.Errors
import grails.test.*
import org.junit.*
import grails.plugin.dao.*
import spock.lang.Specification

@Integration
@Rollback
class JumperDelegateDaoTests extends Specification //FIXME extends BasicTestsForDao
{

	static transactional = false
	
	def jumperDelegateDao
	
	// protected void setUp() {
	// 	//super.setUp()
	// 	//dao = jumperDelegateDao
	// 	//assert dao.domainClass == Jumper
	// }
	
	void testNonTranDao(){
		println "testSave"
		def dom = new Jumper(name:"testSave")
		assert dom.persist()
	}


}

