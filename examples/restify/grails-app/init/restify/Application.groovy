/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

//import org.springframework.context.annotation.ComponentScan

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

//@ComponentScan("restify")
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
    // example of how to scan and pick up the gorm domain that are marked with @entity in the plugin
    @Override
    protected boolean limitScanningToApplication() {
        false
    }
    //
    // // Shows example of how to scan and pick up the gorm domain that are marked with @entity
    // @Override
    // Collection<String> packageNames() {
    //     super.packageNames() + ['restify']
    // }
}
