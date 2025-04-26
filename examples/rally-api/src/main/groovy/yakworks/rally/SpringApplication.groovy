/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally

import java.sql.SQLException

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.h2.tools.Server
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.util.BuildSettings
import yakworks.rally.boot.CacheMgrConfig
import yakworks.rally.boot.RallyApiSpringConfig
import yakworks.rally.boot.WebMvcConfiguration
import yakworks.rest.gorm.RestApiFromConfig

/**
 * Grails adds the @EnableWebMvc to the Application class. This annotation imports DelegatingWebMvcConfiguration and doesn't allow
 * us to import ours. See the WebMvcConfiguration which is needed to create our custom RequestMappingHandlerAdapter.
 */
@ComponentScan(['yakworks.testing.gorm.model'])
@RestApiFromConfig
// caching will use hazelcast for spring caching too, look into how to use caffiene for spring stuff and hazel for hibernate.
@EnableCaching
@EnableAutoConfiguration(
    exclude = [DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class]
)
@Import([RallyApiSpringConfig, WebMvcConfiguration, CacheMgrConfig])
//@SpringBootApplication
@CompileStatic
class SpringApplication extends GrailsAutoConfiguration {

    /** add packages here where the @Entity classes are */
    @Override
    Collection<String> packageNames() {
        super.packageNames() + RallyConfiguration.entityScanPackages + ['yakworks.testing.gorm.model']
    }

    static void main(String[] args) {
        GrailsApp.run(SpringApplication, args)
    }

    // not sure if this is needed but grails adds it to the Application with the transformation
    static {
        System.setProperty(BuildSettings.MAIN_CLASS_NAME, "yakworks.rally.boot.SpringApplication")
    }

    /**
     * Start internal H2 server so we can query the DB from IDE
     *
     * @return H2 Server instance
     * @throws SQLException
     */
    @Profile("server")
    @Lazy(false)
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2Server() throws SQLException {
        println "***********************Starting H2 Server********************"
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9090");
    }

    /*
    grails HibernateDatastoreConnectionSourcesRegistrar.postProcessBeanDefinitionRegistry sets up the dataSource but checks for existing first.
    in order for other spring boot Autoconfigure (such as actuator metrics) to get picked up then datasource needs to be setup early.
    it looks like best solution is to fork or modify the hibernate5 plugin so it uses AutoConfgure and can participate in the normal sboot process
    so instead of using HibernateDatastoreSpringInitializer it can autoconfigure the dataSourceConnectionSourceFactory(CachedDataSourceConnectionSourceFactory)
    and the Datasource early (which looks like it should be a factory based on dataSourceConnectionSourceFactory)
    this is a WIP example.
    */
    //@Bean // @Primary
    // DataSource dataSource() {
    //     return DataSourceBuilder
    //         .create()
    //         .username("sa")
    //         .password("")
    //         .url("jdbc:h2:mem:devDb;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
    //         .driverClassName("org.h2.Driver")
    //         .build();
    // }

    /**
     * To scan and pick up the gorm domains that are marked with @entity
     * outside of the package this Application class is in then this needs to be set to true
     */
    @Override
    protected boolean limitScanningToApplication() { false }

    @SuppressWarnings('Indentation')
    @Override
    @CompileDynamic
    Closure doWithSpring() {{ ->

        //this needs to be here for now until we figure out the config thing
        // openApiGenerator(OpenApiGenerator){
        //     apiSrc = 'api-docs/openapi'
        //     apiBuild = 'api-docs/dist/openapi'
        //     namespaceList = ['rally']
        // }

        //hack to make sure hazel get setup before the one that is setup for hibernates L2 cache as that one
        //is configured to join the name of one already setup in spring.
        def hibernateDatastoreBeanDef = getBeanDefinition('hibernateDatastore')
        // def hazelBeanDef = getBeanDefinition('hazelcastInstance')
        if (hibernateDatastoreBeanDef) {
            // make it depend on hazelcast bean
            hibernateDatastoreBeanDef.dependsOn = ['hazelcastInstance'] as String[]
        }

    }}

}
