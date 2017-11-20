package testing

import grails.test.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification

@Integration
@Rollback
class JumperDelegateDaoSpec extends Specification //FIXME extends BasicTestsForDao
{

	void testNonTranDao(){
		Jumper dom = new Jumper(name:"testSave")

		expect:
		dom.persist()
	}


}

