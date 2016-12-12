package testing

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.validation.Errors
import grails.test.*
import spock.lang.Specification
import testing.*

@Integration
@Rollback
class GormDaoSupportTests extends Specification {

	def grailsApplication
	
	void setup() {
		def dao = grailsApplication.mainContext.gormDaoBean//new GormDaoSupport(Jumper)
		dao.domainClass = Jumper
		assert dao.domainClass == Jumper
		//pop an item in just to have for things below
	}


}

