package yakworks.rally

import gorm.tools.testing.support.GormToolsSpecHelper
import grails.testing.mixin.integration.Integration
import grails.testing.spring.AutowiredTest
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
