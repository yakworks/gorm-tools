package gorm.tools.idgen


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

//import grails.persistence.Entity
@Integration
@Rollback
class JdbcIdGeneratorSpec extends Specification {
    private static final String TABLE_KEY = "Org.id"

    JdbcIdGenerator jdbcIdGenerator
    //JdbcTemplate jdbcTemplate

    void "test getNextId"() {
        when:
        int startVal = jdbcIdGenerator.getNextId(TABLE_KEY, 100)

        then:
        startVal >= jdbcIdGenerator.seedValue //100 is < than seedValue of 1000 so it should get the seed start

        when:
        int i1 = jdbcIdGenerator.getNextId(TABLE_KEY, 50)

        then:
        i1 == (startVal + 100) //eventhough 100 was < seed value it should have incremented it by 100

        when:
        int i2 = jdbcIdGenerator.getNextId(TABLE_KEY, 5)

        then:
        i2 == (startVal + 100 + 50)

        when:
        int i3 = jdbcIdGenerator.getNextId(TABLE_KEY, 1)

        then:
        i3 == (startVal + 100 + 50 + 5)
    }

}
