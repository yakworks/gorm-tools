/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import groovy.transform.CompileStatic

import org.springframework.context.annotation.ComponentScan

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.plugins.metadata.PluginSource

/**
 * THIS SETUP IS ONLY USED FOR INTEGRATION TESTS
 * HACK IN
 */
@ComponentScan(['yakworks.security', 'yakworks.rally'])
@PluginSource
@CompileStatic
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    protected boolean limitScanningToApplication() { false }

    @Override
    Collection<String> packageNames() {
        super.packageNames() + ['yakworks.security', 'yakworks.rally']
    }
}
