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
        def msg = messageSource.getMessage("default.not.found.message", ['Foo', 2] as Object[], Locale.default)

        then:
        msg == "Foo not found with id 2"

    }
    //
    // void "basic look up"(){
    //     expect:
    //     // r.code == "default.not.found.message"
    //     // r.args == ['MockDomain', 2]
    //     "Foo not found for id:2" == messageSource.getMessage("default.not.found.message", ['Foo', 2])
    // }
}
