/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.boot

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteLogger
import org.apache.ignite.Ignition
import org.apache.ignite.cache.spring.IgniteCacheManager
import org.apache.ignite.cache.spring.SpringCacheManager
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.internal.IgnitionEx
import org.apache.ignite.logger.slf4j.Slf4jLogger
import org.apache.ignite.spi.metric.log.LogExporterSpi
import org.apache.ignite.springframework.boot.autoconfigure.IgniteConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

//@AutoConfiguration(after = org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration)
@Configuration(proxyBeanMethods = false)
@Lazy(false)
//@CompileStatic
class IgniteConfig {

    // @Bean
    // public Ignite igniteInstance() {
    //     //return Ignition.start(igniteConfiguration());
    //     return Ignition.start('ignite-config.xml');
    //     return IgnitionEx.start(igniteConfiguration(), "rally-ig")
    // }

    // @Bean
    // public IgniteConfiguration igniteConfiguration() {
    //     // If you provide a whole ClientConfiguration bean then configuration properties will not be used.
    //     IgniteConfiguration cfg = new IgniteConfiguration();
    //     cfg.setIgniteInstanceName("rally-ig");
    //     // IgniteLogger log = new Slf4jLogger();
    //     // cfg.setGridLogger(log);
    //
    //     LogExporterSpi logExporter = new LogExporterSpi();
    //     logExporter.setPeriod(600_000); // Set the period to 10 minutes
    //     // Export only cache metrics
    //     logExporter.setExportFilter(mreg -> mreg.name().startsWith("cache."));
    //
    //     cfg.setMetricExporterSpi(logExporter);
    //
    //     return cfg;
    // }

    @Bean
    public IgniteConfigurer nodeConfigurer() {
        return (cfg) -> {
            //Setting some property.
            //Other will come from `application.yml`
           // cfg.setIgniteInstanceName("my-ignite");
            IgniteLogger log = new Slf4jLogger();
            cfg.setGridLogger(log);
            //can add customization here
            // cfg.setWorkDirectory("build/ignite")
        };
    }

    @Bean
    public IgniteCacheManager igniteCacheManager(Ignite ignite) {
        IgniteCacheManager mgr = new IgniteCacheManager();
        mgr.defaultLockTimeout = 5_000L //in millis
        mgr.igniteInstance = ignite
        //Ignite ignite = Ignition.ignite();
        //Ignition.start("examples/config/example-ignite.xml")) {
        assert ignite
        //mgr.setConfiguration(igniteConfiguration);
        // mgr.setIgniteInstanceName(ignite.name())
        // Other required configuration parameters.

        return mgr;
    }

}
