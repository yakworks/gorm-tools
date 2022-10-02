package yakity.security

import groovy.transform.CompileStatic

import org.springframework.context.annotation.ComponentScan

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

@ComponentScan
@CompileStatic
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
