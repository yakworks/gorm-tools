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
        super.packageNames() + ['yakworks.rally', 'yakworks.testify', 'gorm.tools.security.domain']
    }

}
