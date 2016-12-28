package testing

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class JumperDaoSpec extends Specification {

	static transactional = false
	
	JumperDao jumperDao
	
	def "verify domainClass"() {
		expect:
		jumperDao.domainClass == Jumper
	}
	
	void testNonTranDao(){

		when:
		Jumper dom = new Jumper(name:"testSave")

		then:
		dom.persist([flush: true])
		dom.id != null
	}


}

