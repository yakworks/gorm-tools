/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.support

import groovy.transform.CompileStatic

import grails.config.Config
import grails.core.support.GrailsConfigurationAware

/**
 * Trait that implements the GrailsConfigurationAware and gives access to the config object
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1.12
 */
@CompileStatic
trait ConfigAware implements GrailsConfigurationAware {

    Config config

    @Override
    void setConfiguration(Config co) {
        config = co
    }
}
