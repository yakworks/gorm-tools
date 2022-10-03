package yakity.security

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

@ComponentScan(['yakity.security', 'yakworks.security.gorm'])
// @Import([yakworks.security.config.SpringSecurityConfiguration])
@CompileStatic
class Application extends GrailsAutoConfiguration {
    //@entity scan packages
    List<String> entityScan = ['yakworks.security.gorm']

    static void main(String[] args) {
        System.setProperty("grails.env.standalone", "true")
        GrailsApp.run(Application, args)
    }

    /**
     * To scan and pick up the gorm domains that are marked with @entity
     * outside of the package this Application class is in then this needs to be set to true
     */
    @Override
    protected boolean limitScanningToApplication() {
        false
    }

    @Override
    Collection<String> packageNames() {
        List<String> pkgsNames = packages()*.name
        pkgsNames.addAll( getEntityScan() )
        return pkgsNames
    }
}
