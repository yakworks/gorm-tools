package testing

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
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

