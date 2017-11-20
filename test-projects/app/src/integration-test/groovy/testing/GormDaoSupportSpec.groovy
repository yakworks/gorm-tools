package testing

import grails.core.GrailsApplication
import grails.plugin.dao.GormDaoSupport
import grails.test.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.springframework.validation.Errors
import grails.test.*
import spock.lang.Specification
import testing.*

@Integration
@Rollback
class GormDaoSupportSpec extends Specification {

	GrailsApplication grailsApplication

	def "test gormDaoBean"() {
		given:
		GormDaoSupport dao = grailsApplication.mainContext.gormDaoBean//new GormDaoSupport(Jumper)

		expect:
		dao != null

		when:
		dao.domainClass = Jumper

		then:
		dao.domainClass == Jumper
	}


}

