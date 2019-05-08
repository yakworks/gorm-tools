package repoapp

import org.springframework.context.annotation.ComponentScan

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

@ComponentScan
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

}
