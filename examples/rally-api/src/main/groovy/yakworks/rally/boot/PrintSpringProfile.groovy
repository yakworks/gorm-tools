/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.boot

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * Experiments with profiles
 */
@Component
@CompileStatic
class PrintSpringProfile implements CommandLineRunner {

    @Autowired
    private Environment environment;

    @Value('${foo.message}')
    String message

    @Value('${pprop}')
    String pprop

    @Override
    @SuppressWarnings(['Println'])
    public void run(String... args) throws Exception {

        println "info.app.name: ${environment.getProperty('info.app.name')}"
        println "Active profiles: ${environment.getActiveProfiles()}"
        println "Grails Env: ${grails.util.Environment.current}"
        println "foo.message: ${message}"
        println "testify.message: ${environment.getProperty('testify.message')}"
        println "pprop: ${pprop}"
    }
}
