package gorm.tools

import org.grails.testing.GrailsUnitTest

import gorm.tools.testing.support.ExternalConfigAwareSpec
import spock.lang.Specification

class ExternalConfigAwareSpecTest extends Specification implements GrailsUnitTest, ExternalConfigAwareSpec {

    void  "test external config is loaded"() {
        expect:
        applicationContext.getBean('externalConfigLoader') != null
        config.foo.bar == "test" //this comes from a file defined in config.locations
    }

}
