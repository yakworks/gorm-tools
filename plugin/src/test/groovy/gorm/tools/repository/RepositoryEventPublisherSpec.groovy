package gorm.tools.repository

import gorm.tools.databinding.BindAction
import gorm.tools.repository.events.*
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.events.RepositoryEventType
import gorm.tools.testing.GormToolsTest
import grails.artefact.Artefact
import grails.events.annotation.Subscriber
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

    void "test subscriber listener with persist events"() {
        given:
        Map params = [name: "test"]

        when:
        City city = City.create(params)

        then:
        sleep(1000)
        city.events.beforePersist
        city.events.afterPersist

        when:
        City city2 = new City(id: 1, name: "test")
        city2.persist()

        then:
        sleep(1000)
        city2.events.beforePersist
        city2.events.afterPersist
    }

    void "test events are not raised if using default save()"() {
        setup:
        City city = new City(name: "test")

        when:
        city.save()

        then:
        sleep(1000)
        !city.events.beforeBind
        !city.events.afterBind
        !city.events.beforePersist
        !city.events.afterPersist
    }

    void "test subscriber listener with bind events"() {
        given:
        Map params = [name: "test"]

        when:
        City city = City.create(params)

        then:
        sleep(1000)
        city.events.beforeBind
        city.events.afterBind
    }

    void "test subscriber listener when removing an entity"() {
        given:
        Map params = [name: "test"]

        when:
        City city = City.create(params)
        City city2 = City.create(params)
        city.remove()
        City.removeById(city2.id)

        then:
        sleep(1000)
        city.events.beforeRemove
        city.events.afterRemove
        city2.events.beforeRemove
        city2.events.afterRemove
    }

    void "test subscriber listener when updating an entity"() {
        setup:
        Map params = [id: 1, name: "test"]
        City city = City.create(params)
        city.events = [:]

        when:
        City.update([id: 1, name: "test1"])

        then:
        sleep(1000)
        City.get(1).name == "test1"
        city.events.beforeBind
        city.events.afterBind
        city.events.beforePersist
        city.events.afterPersist

        when:
        city.events = [:]
        city.update([id: 1, name: "test2"])

        then:
        City.get(1).name == "test2"
        city.events.beforeBind
        city.events.afterBind
        city.events.beforePersist
        city.events.afterPersist
    }

}


@Entity
class City {
    String name
    String event
    String eventAfter
    Map<String, Boolean> events = [:]

    static constraints = {
        event nullable:true
        eventAfter nullable:true
    }
}

@Artefact("Repository")
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

    @Subscriber("City.beforeBind")
    void beforeBindSub(BeforeBindEvent e){
        City city = (City) e.entity
        city.events.beforeBind = true
    }

    @Subscriber("City.afterBind")
    void afterBindSub(AfterBindEvent e){
        City city = (City) e.entity
        city.events.afterBind = true
    }

    @Subscriber("City.beforePersist")
    void beforePersistSub(BeforePersistEvent e){
        City city = (City) e.entity
        city.events.beforePersist = true
    }

    @Subscriber("City.afterPersist")
    void afterPersistSub(AfterPersistEvent e){
        City city = (City) e.entity
        city.events.afterPersist = true
    }

    @Subscriber("City.beforeRemove")
    void beforeRemoveSub(BeforeRemoveEvent e){
        City city = (City) e.entity
        city.events.beforeRemove = true
    }

    @Subscriber("City.afterRemove")
    void afterRemoveSub(AfterRemoveEvent e){
        City city = (City) e.entity
        city.events.afterRemove = true
    }

}

