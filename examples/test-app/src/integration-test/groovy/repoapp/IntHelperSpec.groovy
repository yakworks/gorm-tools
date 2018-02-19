package repoapp

import gorm.tools.testing.integration.IntegrationSpecHelper
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import testing.CustomSpecHelper

@Integration
class IntHelperSpec extends Specification implements IntegrationSpecHelper, CustomSpecHelper {

    static List executionOrder = []

    def "check if specificSetup and specificSetupSpec is executed"() {
        expect:
        executionOrder.size() == 2
        executionOrder[0] == "CustomSpecHelper.specificSetupSpec"
        executionOrder[1] == "CustomSpecHelper.specificSetup"

    }

    def "check if specificCleanup is executed"() {
        expect:
        executionOrder.size() == 4
        executionOrder[0] == "CustomSpecHelper.specificSetupSpec"
        executionOrder[1] == "CustomSpecHelper.specificSetup"
        executionOrder[2] == "CustomSpecHelper.specificCleanup"
        executionOrder[3] == "CustomSpecHelper.specificSetup"
    }

    def "check if services are injected"() {
        expect:
        jdbcTemplate != null
        dbDialectService != null
    }

}
