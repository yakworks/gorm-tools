package yakworks.taskify

import org.springframework.context.annotation.ComponentScan

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.plugins.metadata.PluginSource

@ComponentScan
@PluginSource
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    // example of how to scan and pick up the gorm domain that are marked with @entity in the plugin
    @Override
    protected boolean limitScanningToApplication() {
        false
    }

    // in order to pick up the gorm domains that are marked with @Entity this needs to be set.
    @Override
    Collection<String> packageNames() {
        super.packageNames() + ['gorm.tools.security']
    }

}
