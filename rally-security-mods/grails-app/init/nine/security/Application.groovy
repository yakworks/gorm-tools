package nine.security

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.plugins.metadata.PluginSource
import nine.rally.ScanningAutoConfigTrait

@PluginSource
class Application extends GrailsAutoConfiguration implements ScanningAutoConfigTrait {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
