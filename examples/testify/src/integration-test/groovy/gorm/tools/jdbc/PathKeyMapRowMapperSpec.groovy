package gorm.tools.jdbc

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.sql.Sql
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.ResultSet
import java.sql.Statement

@SuppressWarnings(['JdbcResultSetReference', 'JdbcStatementReference'])
@Integration
@Rollback
class PathKeyMapRowMapperSpec extends Specification {

    DataSource dataSource

    void "test mapRow"() {
        setup:
        Sql sql = new Sql(dataSource)
        sql.resultSetConcurrency = java.sql.ResultSet.CONCUR_READ_ONLY
        sql.withStatement { Statement stmt -> stmt.fetchSize = 50 }
        sql.execute("create table MapRowMapperTest(id int not null, value varchar(255) null)")
        sql.executeUpdate("insert into MapRowMapperTest (id, value) values (0, 'test0')")
        sql.executeUpdate("insert into MapRowMapperTest (id, value) values (1, 'test1')")
        PathKeyMapRowMapper rowMapper = new PathKeyMapRowMapper()

        when:
        List<Map> result = []
        sql.query("select * from MapRowMapperTest") { ResultSet resultSet ->
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet, 0))
            }
        }

        then:
        result.size() == 2
        result[0].ID == 0
        result[0].VALUE == 'test0'
        result[1].ID == 1
        result[1].VALUE == 'test1'
    }

}
