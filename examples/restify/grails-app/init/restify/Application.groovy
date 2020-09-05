/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import org.springframework.context.annotation.ComponentScan

import gorm.tools.rest.RestApiFromConfig
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

// the component scan here does not seem to be the same as the packageNames and is needed to pick up the
// the services marked with @Component
@ComponentScan("yakworks.taskify")
@RestApiFromConfig
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
    // Tto scan and pick up the gorm domain that are marked with @entity in the plugin set this
    @Override
    protected boolean limitScanningToApplication() {
        false
    }

    // in order to pick up the gorm domains that are marked with @Entity, need to add packages here
    @Override
    Collection<String> packageNames() {
        super.packageNames() + ['yakworks.taskify', 'gorm.tools.security.domain']
    }

}
