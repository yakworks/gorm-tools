package repoapp

import gorm.tools.testing.integration.ControllerIntegrationSpecHelper
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class OrgControllerSpec extends Specification implements ControllerIntegrationSpecHelper {

    String controllerName = "Org"

    def "test autowire"() {
        setup:
        def controller = new OrgController()

        expect:
        controller.orgRepo == null

        when:
        controller = autowire(controller)

        then:
        controller.orgRepo != null
    }

    def "test mockRender"() {
        setup:
        def controller = new OrgController()

        expect:
        !controller.hasProperty("renderArgs")

        when:
        mockRender(controller)
        controller.renderOrgTemplate()

        then:
        controller.renderArgs.template == "orgTemplate"
    }

    def "check if controller name is specified"() {
        expect:
        getCurrentRequestAttributes().getControllerName() == "Org"
    }

    def "check if services are injected"() {
        expect:
        jdbcTemplate != null
        dbDialectService != null
        ctx != null
    }

}
