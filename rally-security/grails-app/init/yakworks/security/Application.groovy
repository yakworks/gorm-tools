/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import org.springframework.context.annotation.ComponentScan

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.plugins.metadata.PluginSource

/**
 * In place for integration testing
 */
@ComponentScan(['yakworks.rally', 'yakworks.security'])
@PluginSource
class Application extends GrailsAutoConfiguration { // implements ScanningAutoConfigTrait {
    List commonScanPackages = [ 'yakworks.security', 'yakworks.rally']
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    /**
     * add packages here where the other grails artifacts exist such as domains marked with @Entity
     */
    Collection<String> packageNames() {
        def pkgsNames = packages()*.name
        return pkgsNames + commonScanPackages
    }

    /**
     * To scan and pick up the gorm artifacts such as domains that are marked with @entity
     * outside of the package this Application class is in then this needs to be set to true
     */
    boolean limitScanningToApplication() { false }
}
