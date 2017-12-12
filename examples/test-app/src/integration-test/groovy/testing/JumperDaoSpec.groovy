package testing

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
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

