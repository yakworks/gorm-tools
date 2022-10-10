package yakity.security

import groovy.transform.CompileStatic

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

@ComponentScan(['yakity.security', 'yakworks.security'])
@Import([AppSecurityConfiguration])
@CompileStatic
class Application extends GrailsAutoConfiguration {
    //@Entity scan packages to include in additions to this Application class's package
    List<String> artifactScan = ['yakworks.security.gorm']

    static void main(String[] args) {
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
