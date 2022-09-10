package gorm.tools.jdbc

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.jdbc.core.ColumnMapRowMapper
import spock.lang.Specification
import yakworks.gorm.testing.integration.DataIntegrationTest

import javax.sql.DataSource

@Integration
@Rollback
class ScrollableQuerySpec extends Specification implements DataIntegrationTest {

    DataSource dataSource

    void "test eachRow"() {
        setup:
        String createTableQuery = "create table ScrollableQueryTest(id int not null, value varchar(255) null)"
        jdbcTemplate.execute(createTableQuery)
        (0..1).each { insertTestRecord(it, "test${it}") }
        ScrollableQuery scrollableQuery = new ScrollableQuery(new ColumnMapRowMapper(), dataSource, 50)

        when:
        List values = []
        scrollableQuery.eachRow("select * from ScrollableQueryTest") { row -> values.add(row.value) }

        then:
        values.size() == 2
        values.contains('test0')
        values.contains('test1')
    }

    void "test eachBatch"() {
        setup:
        (0..9).each { insertTestRecord(it, "test${it}") }
        ScrollableQuery scrollableQuery = new ScrollableQuery(new ColumnMapRowMapper(), dataSource, 50)

        when:
        List batches = []
        scrollableQuery.eachBatch("select * from ScrollableQueryTest", 5) { batch -> batches.add(batch) }

        then:
        //two batches
        batches.size() == 2
        //batch contains 5 records
        batches[0].size() == 5
        batches[1].size() == 5

    }

    void "test rows"() {
        setup:
        ScrollableQuery scrollableQuery = new ScrollableQuery(new ColumnMapRowMapper(), dataSource, 50)
        (0..2).each { insertTestRecord(it, 'test') }
        (3..5).each { insertTestRecord(it, 'test1') }

        when:
        List values = scrollableQuery.rows("select * from ScrollableQueryTest where value='test'")

        then:
        values.size() == 3
    }

    private void insertTestRecord(int id, String value) {
        jdbcTemplate.execute("insert into ScrollableQueryTest (id, value) values (${id}, '${value}')")
    }

}
