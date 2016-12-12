package testing

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
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
		when:
		def dom = new Jumper(name:"testSave")
		then:
		dom.persist([flush: true])
		dom.id != null
	}


}

