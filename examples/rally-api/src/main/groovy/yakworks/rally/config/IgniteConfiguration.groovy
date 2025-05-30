/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.config

import groovy.transform.CompileStatic

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteSpring
import org.apache.ignite.cache.spring.IgniteCacheManager
import org.apache.ignite.failure.NoOpFailureHandler
import org.apache.ignite.spi.metric.log.LogExporterSpi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile

/**
 * Example for setting up Ignite as alternative to Hazelcast.
 * Its a viable replacement except we would need to reproduce the spring-session (if we still need it)
 */
@Configuration(proxyBeanMethods = false)
@Profile("ignite")
@Lazy(false)
@CompileStatic
class IgniteConfiguration {

    @Autowired ApplicationContext applicationContext

    // @Bean
    // public Ignite igniteInstance() {
    //     //return Ignition.start(igniteConfiguration());
    //     return Ignition.start('ignite-config.xml');
    //     return IgnitionEx.start(igniteConfiguration(), "rally-ig")
    // }

    @Bean
    public org.apache.ignite.configuration.IgniteConfiguration igniteConfiguration() {
        // If you provide a whole ClientConfiguration bean then configuration properties will not be used.
        org.apache.ignite.configuration.IgniteConfiguration cfg = new org.apache.ignite.configuration.IgniteConfiguration();
        cfg.setIgniteInstanceName("rally-ig");
        cfg.failureHandler = new NoOpFailureHandler()
        // IgniteLogger log = new Slf4jLogger();
        // cfg.setGridLogger(log);
        //cfg.workDirectory = BuildSupport.projectPath.toAbsolutePath().resolve("build/ignite")

        LogExporterSpi logExporter = new LogExporterSpi();
        logExporter.setPeriod(600_000); // Set the period to 10 minutes
        // Export only cache metrics
        logExporter.setExportFilter(mreg -> mreg.name().startsWith("cache."));
        cfg.setMetricExporterSpi(logExporter);

        return cfg;
    }

    @Bean
    public Ignite igniteInstance(org.apache.ignite.configuration.IgniteConfiguration igniteConfiguration) {
        //return Ignition.start(igniteConfiguration());
        // return Ignition.start('ignite-config.xml');
        //return IgnitionEx.start(igniteConfiguration(), "rally-ig")
        return IgniteSpring.start(igniteConfiguration, applicationContext)
        // var igins = new IgniteSpringBean()
        // igins.configuration = igniteConfiguration
        // return igins
    }

    // @Bean
    // public IgniteConfigurer nodeConfigurer() {
    //     return (cfg) -> {
    //         //Setting some property.
    //         //Other will come from `application.yml`
    //        // cfg.setIgniteInstanceName("my-ignite");
    //         IgniteLogger log = new Slf4jLogger();
    //         cfg.setGridLogger(log);
    //         //can add customization here
    //         //cfg.workDirectory = BuildSupport.projectPath.toAbsolutePath().resolve("build/ignite")
    //
    //     };
    // }

    @Bean
    //@DependsOn(["igniteInstance"])
    public IgniteCacheManager gridCacheManager(Ignite ignite) {
        IgniteCacheManager mgr = new IgniteCacheManager();
        mgr.defaultLockTimeout = 5_000L //in millis
        mgr.igniteInstance = ignite
        //Ignite ignite = Ignition.ignite();
        //Ignition.start("examples/config/example-ignite.xml")) {
        // assert ignite
        //mgr.setConfiguration(igniteConfiguration);
        // mgr.setIgniteInstanceName(ignite.name())
        // Other required configuration parameters.

        return mgr;
    }

}
