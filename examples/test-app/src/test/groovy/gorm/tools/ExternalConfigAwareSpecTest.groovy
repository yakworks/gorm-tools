import gorm.tools.testing.unit.ExternalConfigAwareSpec
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification


class ExternalConfigAwareSpecTest extends Specification implements ExternalConfigAwareSpec, GrailsUnitTest {

    void  "test external config is loaded"() {
        expect:
        config.grails.plugin.gormtools.test == "test"
    }
}