/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.support

import groovy.transform.CompileDynamic

import gorm.tools.testing.support.ExternalConfigLoader

/**
 * The trait makes it possible to load external config during unit tests.
 * If external-config plugin is installed, the configuration defined in config.locations will be
 * loaded and be made available to unit tests.
 */

@CompileDynamic
trait ExternalConfigAwareSpec {

    /**
     * externalConfigLoader is defined using doWithSpring so that it runs during Grails application lifecycle
     * and makes the config available to GrailsConfigurationAware etc.
     *
     * If the test defines its own doWithSpring - it needs to call ExternalConfigAwareSpec.super.doWithSpring
     *
     */
    Closure doWithSpring() {
        return { ->
            externalConfigLoader(ExternalConfigLoader)
        }
    }
}
