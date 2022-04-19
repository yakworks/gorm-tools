package nine.security

import org.springframework.context.annotation.ComponentScan

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.plugins.metadata.PluginSource

/**
 * In place for integration testing
 */
@ComponentScan(['yakworks.security'])
@PluginSource
class Application extends GrailsAutoConfiguration { // implements ScanningAutoConfigTrait {
    List commonScanPackages = [ 'gorm.tools.security', 'yakworks.rally', 'yakworks.security',
                                'nine.rally', 'nine.security']
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
