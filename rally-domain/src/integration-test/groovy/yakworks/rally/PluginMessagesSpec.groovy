package yakworks.rally


import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.i18n.icu.ICUMessageSource

/**
 * sanity check that messages are picked up in plugins
 */
@Integration
class PluginMessagesSpec extends Specification {

    ICUMessageSource messageSource

    void "messageSource lookup"(){
        expect:
        messageSource instanceof ICUMessageSource
        "Data Problem" == messageSource.get("error.data.problem")

    }
}
