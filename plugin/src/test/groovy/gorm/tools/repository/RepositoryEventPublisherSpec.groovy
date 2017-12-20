package gorm.tools.repository

import gorm.tools.databinding.BindAction
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.events.RepositoryEventType
import gorm.tools.testing.GormToolsTest
import grails.persistence.Entity
import spock.lang.Specification

class RepositoryEventPublisherSpec extends Specification implements GormToolsTest {

    void setup() {
        mockDomain(City)
    }

    RepoEventPublisher repoEventPublisher

    void testEventsFired() {
        given:
        Map params = [id: 1, name: "test"]

        when:
        City city = City.create(params)

        then:
        city != null
        city.event == "beforeBind Create"
        city.eventAfter == "afterBind Create"

        when:
        city = City.update(params)

        then:
        city != null
        city.event == "beforeBind Update"
        city.eventAfter == "afterBind Update"
    }

    void testInvokeEvent() {
        given:
        City city = new City()
        Map params = [name: "test"]

        when:
        repoEventPublisher.invokeEventMethod(City.repo, RepositoryEventType.BeforeBind.eventKey, city, params, BindAction.Create)

        then:
        city.event == "beforeBind Create"

        when:
        repoEventPublisher.invokeEventMethod(City.repo, RepositoryEventType.AfterBind.eventKey, city, params, BindAction.Update)

        then:
        city.eventAfter == "afterBind Update"

        when:
        repoEventPublisher.invokeEventMethod(City.repo, RepositoryEventType.BeforeRemove.eventKey, city, params)

        then:
        city.event == "beforeRemove"

        when:
        repoEventPublisher.invokeEventMethod(City.repo, RepositoryEventType.AfterRemove.eventKey, city, params)

        then:
        city.event == "afterRemove"
    }
}


@Entity
class City {
    String name
    String event
    String eventAfter
}

class CityRepo implements GormRepo<City> {

    void beforeBind(City city, Map params, BindAction ba) {
        city.event = "beforeBind ${ba.name()}"
    }

    void afterBind(City city, Map params, BindAction ba) {
        city.eventAfter = "afterBind ${ba.name()}"
    }

    void beforeRemove(City city, Map params) {
        city.event = "beforeRemove"
    }

    void afterRemove(City city, Map params) {
        city.event = "afterRemove"
    }

}

