package gpbench.services

import java.sql.Connection
import javax.sql.DataSource

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.stereotype.Component

import gorm.tools.repository.GormRepo
import gpbench.model.Country
import gpbench.model.Region

@Component
@CompileStatic
class DataSetup implements gpbench.services.traits.BenchConfig {

    @Autowired
    JdbcTemplate jdbcTemplate
    @Autowired
    DataSource dataSource

    @CompileDynamic
    void initBaseData() {
        truncateTables()
        executeSqlScript("test-tables.sql")
        List<List<Map>> countries = csvReader.read("Country").collate(batchSize)
        List<List<Map>> regions = csvReader.read("Region").collate(batchSize)
        insert(countries, Country.repo)
        insert(regions, Region.repo)

        assert Country.count() == 275
        assert Region.count() == 3953
    }

    void insert(List<List<Map>> batchList, GormRepo repo) {
        parallelTools.each(batchList) { List<Map> list ->
            batchCreate(repo, list)
        }
    }

    void executeSqlScript(String file) {
        Resource resource = grailsApplication.mainContext.getResource("classpath:$file")
        assert resource.exists()
        Connection connection = dataSource.getConnection()
        ScriptUtils.executeSqlScript(connection, resource)
    }

    void truncateTables() {
        // jdbcTemplate.update("DELETE FROM Origin")
        // jdbcTemplate.update("DELETE FROM city")
        jdbcTemplate.update("DELETE FROM Region")
        jdbcTemplate.update("DELETE FROM Country")
    }
}
