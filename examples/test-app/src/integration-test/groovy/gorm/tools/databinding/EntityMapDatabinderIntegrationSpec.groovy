package gorm.tools.databinding

import grails.testing.mixin.integration.Integration
import repoapp.Org
import spock.lang.Specification

@Integration
class EntityMapDatabinderIntegrationSpec extends Specification {

    EntityMapBinder entityMapBinder

    void "test non bindable property"() {
        given:
        Org org = new Org()

        when:
        entityMapBinder.bind(org, [name:"test", event:"test"])

        then:
        org.name == "test"
        org.event == null
    }
}
