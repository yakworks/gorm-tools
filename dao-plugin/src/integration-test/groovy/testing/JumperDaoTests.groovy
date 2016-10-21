package testing

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.validation.Errors
import grails.test.*
import grails.plugin.dao.*
import spock.lang.Specification

@Integration
@Rollback
class JumperDaoTests extends Specification {

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

