package gpbench.helpers

import grails.core.GrailsApplication
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.stereotype.Component

import javax.sql.DataSource
import java.sql.Connection

@Component
@CompileStatic
class BenchmarkHelper {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    GrailsApplication grailsApplication

    @Autowired
    DataSource dataSource


    void executeSqlScript(String file) {
        Resource resource = grailsApplication.mainContext.getResource("classpath:$file")
        assert resource.exists()
        Connection connection = dataSource.getConnection()
        ScriptUtils.executeSqlScript(connection, resource)
    }

    void truncateTables() {
        jdbcTemplate.update("DELETE FROM origin")
        jdbcTemplate.update("DELETE FROM city")
        jdbcTemplate.update("DELETE FROM region")
        jdbcTemplate.update("DELETE FROM country")
    }
}
