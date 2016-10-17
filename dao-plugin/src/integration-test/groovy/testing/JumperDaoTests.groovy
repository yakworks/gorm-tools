package testing

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.validation.Errors
import grails.test.*
import grails.plugin.dao.*

@Integration
@Rollback
class JumperDaoTests extends BasicTestsForDao {

	static transactional = false
	
	JumperDao jumperDao
	
	void setup() {
		def dao = jumperDao
		assert dao.domainClass == Jumper
	}
	
	void testNonTranDao(){
		println "testSave"
		def dom = new Jumper(name:"testSave")
		assert dom.persist()
	}


}

