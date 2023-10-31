/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.config

import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

import yakworks.rally.extensions.AppTimeZone

// @Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix="app")
// @ConfigurationPropertiesScan
@CompileStatic
class AppRallyConfig {
    String hello = "world"

    String resourcesDir

    AppDefaultsConfig defaults = new AppDefaultsConfig()

    static class AppDefaultsConfig {
        /**
         * The system zone will normally be set to UTC.
         * The App can have a different default time zone for the majority of the users and accounting is anchored on.
         * A GL posting date for example is usually anchored to a timezone for balancing purposes.
         */
        TimeZone timeZone = TimeZone.getTimeZone("America/New_York")

        /**
         * The default or primary currency used in the system.
         */
        Currency currency //= Currency.getInstance('USD')
    }

    @PostConstruct
    void init(){
        AppTimeZone.timeZone = defaults.timeZone
    }
}
