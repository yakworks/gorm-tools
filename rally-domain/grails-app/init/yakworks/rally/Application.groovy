/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally

import java.time.Duration
import java.time.Instant

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.plugins.metadata.PluginSource

/** NOTE: Reminder, this Application class is used mainly for Grails integration tests,
 * not really for deploy
 */
@Import([RallyConfiguration])
//@ComponentScan(['yakworks.rally']) //scan and pick up all
@ComponentScan(['yakworks.testing.gorm.model'])
@PluginSource
class Application extends GrailsAutoConfiguration {

    private static Instant startTime;
    private static Instant endTime;

    @SuppressWarnings(['Println', 'UnnecessaryToString', 'AssignmentToStaticFieldFromInstanceMethod'])
    static void main(String[] args) {
        startTime = Instant.now();
        GrailsApp.run(Application, args)
        println("Total time taken in start up " + Duration.between(startTime, endTime).toString());
    }

    @EventListener(ApplicationReadyEvent.class)
    void startApp() {
        endTime = Instant.now();
    }

    /**
     * To scan and pick up the gorm artifacts such as gorm entities that are marked with @entity
     * outside of the package this Application class is in then this needs to be set to true
     */
    @Override
    protected boolean limitScanningToApplication() { false }

    /**
     * add packages here where the other grails artifacts exist such as domains marked with @Entity
     */
    @Override
    Collection<String> packageNames() {
        super.packageNames() + RallyConfiguration.entityScanPackages + ['yakworks.testing.gorm.model']
    }

}
