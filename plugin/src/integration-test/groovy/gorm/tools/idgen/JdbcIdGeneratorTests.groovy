package gorm.tools.idgen

import grails.gorm.transactions.Rollback
import grails.plugin.gormtools.Application
import grails.testing.mixin.integration.Integration
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

import javax.annotation.Resource

@Rollback
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Integration(applicationClass = Application.class)
class JdbcIdGeneratorTests extends GroovyTestCase {
    private static final String TABLE_KEY = "Custom1.id"

    @Autowired
    JdbcIdGenerator jdbcIdGenerator
    JdbcTemplate jdbcTemplate

    static int startVal
    static int nextIdSIEndVal

    @Test
    void testBGetNextId_String_Int() {
        IdGeneratorTestHelper.createTables(jdbcTemplate)
        startVal = jdbcIdGenerator.getNextId(TABLE_KEY, 100)
        assert startVal == jdbcIdGenerator.seedValue //100 is < than seedValue of 1000 so it should get the seed start
        int i1 = jdbcIdGenerator.getNextId(TABLE_KEY, 50)
        assert i1 == jdbcIdGenerator.seedValue + 100 //eventhough 100 was < seed value it should have incremented it by 100
        int i2 = jdbcIdGenerator.getNextId(TABLE_KEY, 5)
        assert i2 == jdbcIdGenerator.seedValue + 100 + 50
        int i3 = jdbcIdGenerator.getNextId(TABLE_KEY, 1)
        assert i3 == jdbcIdGenerator.seedValue + 100 + 50 + 5

        nextIdSIEndVal = i3
    }

}
