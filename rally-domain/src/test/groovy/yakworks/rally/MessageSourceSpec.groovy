package yakworks.rally

import org.grails.testing.GrailsUnitTest

import gorm.tools.testing.support.GormToolsSpecHelper
import grails.testing.spring.AutowiredTest
import spock.lang.Specification
import yakworks.i18n.icu.GrailsICUMessageSource
import yakworks.i18n.icu.ICUMessageSource

/**
 * sanity check that messages are picked up in plugins
 */
class MessageSourceSpec extends Specification implements GormToolsSpecHelper, AutowiredTest {

    ICUMessageSource messageSource

    void setupSpec() {
        defineCommonBeans()
    }

    void "messageSource lookup"(){
        when:
        assert messageSource instanceof ICUMessageSource
        def msg = messageSource.get("error.data.problem")

        then:
        msg == "Data Problem"

    }
}
