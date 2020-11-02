package yakworks.taskify

import org.springframework.context.annotation.ComponentScan

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.plugins.metadata.PluginSource

@ComponentScan(['yakworks.taskify', 'gorm.tools.audit'])
@PluginSource
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    /**
     * To scan and pick up the gorm artifacts such as domains that are marked with @entity
     * outside of the package this Application class is in then this needs to be set to true
     */
    @Override
    protected boolean limitScanningToApplication() { false }

    /**
     * add packages here where the other grails artifacts exist such as domains marked with @Entity
     */
    @Override
    Collection<String> packageNames() {
        super.packageNames() + ['gorm.tools.security']
    }

}
