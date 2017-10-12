package gorm.tools.idgen

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.springframework.jdbc.core.JdbcTemplate
import grails.plugin.dao.Application

import javax.annotation.Resource

@Rollback
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Integration(applicationClass = Application.class)
public class JdbcIdGeneratorTests extends GroovyTestCase {
	private static final String TABLE_KEY = "Custom1.id"
	@Resource IdGenerator jdbcIdGenerator
	JdbcTemplate jdbcTemplate
	static int startVal
	static int nextIdEndVal
	static int nextIdSIEndVal
	static boolean transactional=true

	@Test
	public void testAGetNextId_String() {
		startVal = jdbcIdGenerator.getNextId(TABLE_KEY)
		int i1 = jdbcIdGenerator.getNextId(TABLE_KEY)
		int i2 = jdbcIdGenerator.getNextId(TABLE_KEY)
		int i3 = jdbcIdGenerator.getNextId(TABLE_KEY)
		assert i1+1 == i2
		assert i1+2 == i3
		nextIdEndVal = i3
	}

	@Test
	public void testBGetNextId_String_Int() {
		int resultCode= jdbcTemplate.update("update Custom1 set Version=20")
		int i1 = jdbcIdGenerator.getNextId(TABLE_KEY, 50)
		int i2 = jdbcIdGenerator.getNextId(TABLE_KEY, 5)
		int i3 = jdbcIdGenerator.getNextId(TABLE_KEY, 1)
		assert i1+50 ==  i2
		assert i1+55 == i3
		nextIdSIEndVal = i3
	}

	@Test
	public void testCTransactionalBehavior() {
		println "WARNING!"
		println "This test must run last in every test run."

		assert startVal + 3 == nextIdEndVal
		assert startVal + 59 == nextIdSIEndVal
	}
}
