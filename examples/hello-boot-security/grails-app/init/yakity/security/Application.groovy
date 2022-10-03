package yakity.security

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

@ComponentScan(['yakity.security', 'yakworks.security'])
// @Import([yakworks.security.config.SpringSecurityConfiguration])
@CompileStatic
class Application extends GrailsAutoConfiguration {
    //@Entity scan packages
    List<String> artifactScan = ['yakworks.security.gorm']

    static void main(String[] args) {
        System.setProperty("grails.env.standalone", "true")
        GrailsApp.run(Application, args)
    }

    /**
     * true by default for some reason.
     */
    @Override
    protected boolean limitScanningToApplication() {
        false
    }

    @Override
    Collection<String> packageNames() {
        List<String> pkgsNames = packages()*.name
        pkgsNames.addAll( getArtifactScan() )
        return pkgsNames
    }
}
