package gorm.tools.repository

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
        city.region == "beforeCreate"

        when:
        city = City.update(params)

        then:
        city != null
        city.region == "afterUpdate"
    }

    void testInvokeEvent() {
        given:
        City city = new City()
        Map params = [name: "test"]

        when:
        repoEventPublisher.invokeEventMethod(City.repo, RepositoryEventType.BeforeUpdate.eventKey, city, params)

        then:
        city.region == "beforeUpdate"

        when:
        repoEventPublisher.invokeEventMethod(City.repo, RepositoryEventType.AfterUpdate.eventKey, city, params)

        then:
        city.region == "afterUpdate"

        when:
        repoEventPublisher.invokeEventMethod(City.repo, RepositoryEventType.BeforeRemove.eventKey, city, params)

        then:
        city.region == "beforeRemove"

        when:
        repoEventPublisher.invokeEventMethod(City.repo, RepositoryEventType.AfterRemove.eventKey, city, params)

        then:
        city.region == "afterRemove"
    }
}


@Entity
class City {
    String name
    String region
}

class CityRepo implements GormRepo<City> {

    void beforeCreate(City city, Map params) {
        city.region = "beforeCreate"
    }

    void beforeUpdate(City city, Map params) {
        city.region = "beforeUpdate"
    }

    void afterUpdate(City city, Map params) {
        city.region = "afterUpdate"
    }

    void beforeRemove(City city, Map params) {
        city.region = "beforeRemove"
    }

    void afterRemove(City city, Map params) {
        city.region = "afterRemove"
    }

}

