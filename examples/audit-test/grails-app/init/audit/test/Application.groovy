package audit.test

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import yakworks.rally.RallyConfiguration

@ComponentScan(['yakworks.testify', 'yakworks.testing.gorm.model'])
@Import([RallyConfiguration])
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
