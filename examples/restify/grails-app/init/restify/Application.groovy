/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import javax.sql.DataSource

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

import gorm.tools.rest.RestApiFromConfig
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

// the component scan here does not seem to be the same as the packageNames and is needed to pick up the
// the services marked with @Component
@ComponentScan(['yakworks.testify', 'gorm.tools.security', 'yakworks.rally', 'yakworks.gorm.testing.model'])
@RestApiFromConfig
@EnableAsync
@EnableScheduling
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    /**
     * To scan and pick up the gorm domains that are marked with @entity
     * outside of the package this Application class is in then this needs to be set to true
     */
    @Override
    protected boolean limitScanningToApplication() { false }

    /**
     * add packages here where the other grails artifacts exist such as domains marked with @Entity
     */
    @Override
    Collection<String> packageNames() {
        super.packageNames() + ['yakworks.rally', 'yakworks.testify', 'gorm.tools.security', 'yakworks.gorm.testing.model']
    }

    @Autowired DataSource dataSource

    @Bean
    LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime() // Works on Postgres, MySQL, MariaDb, MS SQL, Oracle, DB2, HSQL and H2
                .build()
        );
    }

    @Bean
    TestJob testJob() {
        return new TestJob()
    }

}
