package gorm.tools.idgen

import grails.plugin.dao.Application
import grails.testing.mixin.integration.Integration
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

@Integration(applicationClass = Application.class)
//@Rollback
class BatchIdGenerator2Spec extends Specification {

	JdbcTemplate jdbcTemplate
	private static final String TABLE_KEY = "Custom1.id"
	IdGenerator idGenerator
	static int startVal
	static int nextIdEndVal
	static int nextIdSIEndVal

	void "test getNextId" () {
		setup:
		IdGeneratorTestHelper.createTables(jdbcTemplate)

		when:
		int resultCode= jdbcTemplate.update("update Custom1 set Version=20")
		startVal = idGenerator.getNextId(TABLE_KEY)
		int i1 = idGenerator.getNextId(TABLE_KEY)
		int i2 = idGenerator.getNextId(TABLE_KEY)
		int i3 = idGenerator.getNextId(TABLE_KEY)

		then:
		i1+1 == i2
		i1+2 == i3
	}

}
