package yakworks.testify

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.plugins.metadata.PluginSource
import yakworks.rally.RallyConfiguration

/** NOTE: Reminder, this Application class is used mainly for Grails integration tests, not really for deploy */
@ComponentScan(['yakworks.testify', 'yakworks.testing.gorm.model'])
@Import([RallyConfiguration])
@PluginSource
class Application extends GrailsAutoConfiguration {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    /**
     * add packages here where the other grails artifacts exist such as domains marked with @Entity
     */
    @Override
    Collection<String> packageNames() {
        super.packageNames() + RallyConfiguration.entityScanPackages + ['yakworks.testing.gorm.model']
    }

    /**
     * To scan and pick up the gorm artifacts such as domains that are marked with @entity
     * outside of the package this Application class is in then this needs to be set to true
     */
    @Override
    protected boolean limitScanningToApplication() { false }


}
