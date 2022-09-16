package yakworks.rally


import spock.lang.Specification
import yakworks.i18n.icu.ICUMessageSource
import yakworks.testing.gorm.unit.DataRepoTest

/**
 * sanity check that messages are picked up in plugins
 */
class MessageSourceSpec extends Specification implements DataRepoTest {

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
