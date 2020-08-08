package gorm.tools

import gorm.tools.testing.support.ExternalConfigAwareSpec
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class ExternalConfigAwareSpecTest extends Specification implements GrailsUnitTest, ExternalConfigAwareSpec {

    void  "test external config is loaded"() {
        expect:
        applicationContext.getBean('externalConfigLoader') != null
        config.grails.plugin.gormtools.test == "test" //this comes from a file defined in config.locations
    }

}
