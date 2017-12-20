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

    void "test subscriber with BeforeBindEvent"() {
        given:
        Map params = [name: "test"]

        when:
        City city = City.create(params)

        then:
        sleep(1000)
        city.eventSub == "BeforeBindEvent"
    }

    void "test subscriber"() {
        given:
        Map params = [name: "test"]

        when:
        City c = City.create(params)
        //repoEventPublisher.doBeforeBind(City.repo, c, params, BindAction.Create)

        then:
        sleep(1000)
        c.eventSubAfter == "XXX"

    }

    //Todo
    @spock.lang.Ignore
    void "test subscriber with BeforeRemoveEvent"() {
        given:
        Map params = [name: "test"]

        when:
        City city = City.create(params)
        city.remove()

        then:
        sleep(1000)
        city.eventSub == "BeforeRemoveEvent"
    }
}


@Entity
class City {
    String name
    String event
    String eventAfter
    String eventSub
    String eventSubAfter

    static constraints = {
        event nullable:true
        eventAfter nullable:true
        eventSub nullable:true
        eventSubAfter nullable:true
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
        city.eventSub = "BeforeBindEvent"
    }

    @Subscriber("City.afterBind")
    void afterBindSub(AfterBindEvent e){
        City city = (City) e.entity
        city.eventSubAfter = "XXX"
    }

    @Subscriber("City.beforeRemove")
    void beforeRemoveSub(BeforeRemoveEvent e){
        City city = (City) e.entity
        city.eventSub = "BeforeRemoveEvent"
    }

}

