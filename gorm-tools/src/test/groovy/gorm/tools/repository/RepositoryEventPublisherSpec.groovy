/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import gorm.tools.databinding.BindAction
import gorm.tools.repository.RepoEntity
import gorm.tools.repository.events.*
import gorm.tools.testing.TestDataJson
import static gorm.tools.testing.TestDataJson.buildCreate
import gorm.tools.testing.unit.DataRepoTest
import grails.persistence.Entity
import org.springframework.context.event.EventListener
import spock.lang.Specification

class RepositoryEventPublisherSpec extends Specification implements DataRepoTest {

    RepoEventPublisher repoEventPublisher

    void setup() {
        mockDomain(City)
    }

    void testEventsFired() {
        when:
        City city = TestDataJson.buildCreate(City)//City.create(params)

        then:
        city != null
        city.event == "beforeBind Create"
        city.eventAfter == "afterBind Create"
        city.beforePersistRepoEvent.bindAction == BindAction.Create
        city.afterPersistRepoEvent.bindAction == BindAction.Create
        city.beforePersistRepoEvent.args.foo == 'beforePersist'
        //city.beforePersistRepoEvent.data
        //city.afterPersistRepoEvent.data

    }

    void "test persist events from update"() {
        when:
        TestDataJson.buildCreate(City) //make sure one is there
        Map tdata = [id: 1, name: "test update"]
        City city = City.update(tdata)

        then:
        city.name == "test update"
        city.event == "beforeBind Update"
        city.eventAfter == "afterBind Update"
        city.beforePersistRepoEvent.bindAction == BindAction.Update
        city.afterPersistRepoEvent.bindAction == BindAction.Update
        city.beforePersistRepoEvent.data == tdata
        city.afterPersistRepoEvent.data == tdata
        city.beforePersistRepoEvent.args.foo == 'beforePersist'
    }

    void "test persist events are setup properly"() {

        when:
        City cm = new City( name: "from scratch")

        then:
        cm.persist(failOnError:true, fooBar:'baz')
        cm.name == "from scratch"
        cm.beforePersistRepoEvent instanceof BeforePersistEvent
        cm.afterPersistRepoEvent instanceof AfterPersistEvent
        cm.beforePersistRepoEvent.bindAction == null
        cm.afterPersistRepoEvent.bindAction == null
        cm.afterPersistRepoEvent.args.fooBar == 'baz'
    }

    void testInvokeEvent() {
        given:
        City city = new City()
        Map params = [name: "test"]

        when:
        BeforeBindEvent bbe = new BeforeBindEvent(City.repo, city, params, BindAction.Create, [:])
        repoEventPublisher.publishEvents(City.repo, bbe, [city, params, bbe] as Object[])

        then:
        city.event == "beforeBind Create"

        when:
        AfterBindEvent abe = new AfterBindEvent(City.repo, city, params, BindAction.Update, [:])
        repoEventPublisher.invokeEventMethod(City.repo, RepositoryEventType.AfterBind.eventKey, city, params, abe)

        then:
        city.eventAfter == "afterBind Update"

        when:
        BeforeRemoveEvent bre = new BeforeRemoveEvent(City.repo, city,[:])
        repoEventPublisher.invokeEventMethod(City.repo, RepositoryEventType.BeforeRemove.eventKey, city, bre)

        then:
        city.event == "beforeRemove"

        when:
        AfterRemoveEvent are = new AfterRemoveEvent(City.repo, city,[:])
        repoEventPublisher.invokeEventMethod(City.repo, RepositoryEventType.AfterRemove.eventKey, city, are)

        then:
        city.event == "afterRemove"
    }

    void "test subscriber listener with persist events"() {
        when:
        City city = buildCreate(City) //City.create([name: "test"])

        then:
        sleep(100)
        city.events.beforePersist
        city.events.afterPersist

        when:
        City city2 = build(City)//new City(id: 1, name: "test")
        city2.persist()

        then:
        sleep(100)
        city2.events.beforePersist
        city2.events.afterPersist
    }

    void "test events are not raised if using default .save"() {
        setup:
        City city = new City(name: "test")

        when:
        city.save()

        then:
        sleep(100)
        !city.events.beforeBind
        !city.events.afterBind
        !city.events.beforePersist
        !city.events.afterPersist
    }

    void "test subscriber listener with bind events"() {
        when:
        City city = buildCreate(City)

        then:
        sleep(100)
        city.events.beforeBind
        city.events.afterBind
    }

    void "test subscriber listener when removing an entity"() {
        when:
        City city = buildCreate(City) //City.create(params)
        City city2 = buildCreate(City) //City.create(params)
        city.remove()
        City.removeById(city2.id)

        then:
        sleep(100)
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
        sleep(100)
        City.get(1).name == "test1"
        city.events.beforeBind
        city.events.afterBind
        city.events.beforePersist
        city.events.afterPersist

        when:
        city.events = [:]
        City.update([id: 1, name: "test2"])

        then:
        City.get(1).name == "test2"
        city.events.beforeBind
        city.events.afterBind
        city.events.beforePersist
        city.events.afterPersist
    }

    void "test changing entity in listener with calling default save"() {
        given:
        Map params = [name: "test", name2: "test2"]

        when:
        City.create(params)

        then:
        City.get(1).name2 == "name2"
    }

}


@Entity @RepoEntity
class City {
    String name
    String name2
    String event
    String eventAfter
    Map<String, Boolean> events = [:]
    RepositoryEvent beforePersistRepoEvent
    RepositoryEvent afterPersistRepoEvent
    RepositoryEvent beforeBindRepoEvent
    RepositoryEvent afterBindRepoEvent

    static constraints = {
        name2 nullable:true
        event nullable:true
        eventAfter nullable:true
        beforePersistRepoEvent nullable:true
        afterPersistRepoEvent nullable:true
        beforeBindRepoEvent nullable:true
        afterBindRepoEvent nullable:true
    }
}

@GormRepository
class CityRepo implements GormRepo<City> {


    @RepoListener
    void beforePersist(City city, BeforePersistEvent e) {
        e.args['foo'] = 'beforePersist'
        city.beforePersistRepoEvent = e
    }


    @RepoListener
    void afterPersist(City city, AfterPersistEvent e) {
        assert e.args['foo'] == 'beforePersist'
        city.afterPersistRepoEvent = e
    }

    @RepoListener
    void beforeBind(City city, Map data, BeforeBindEvent e) {
        city.event = "beforeBind ${e.bindAction}"
        city.beforeBindRepoEvent = e
    }

    @RepoListener
    void afterBind(City city, Map data, AfterBindEvent e) {
        city.eventAfter = "afterBind ${e.bindAction}"
        city.afterBindRepoEvent = e
    }

    @RepoListener
    void beforeRemove(City city, BeforeRemoveEvent e) {
        city.event = "beforeRemove"
    }

    @RepoListener
    void afterRemove(City city, AfterRemoveEvent e) {
        city.event = "afterRemove"
    }

    @EventListener
    void beforeBindSub(BeforeBindEvent e){
        City city = (City) e.entity
        city.events.beforeBind = true
    }

    @EventListener
    void afterBindSub(AfterBindEvent e){
        City city = (City) e.entity
        city.events.afterBind = true
    }

    @EventListener
    void beforePersistSub(BeforePersistEvent e){
        City city = (City) e.entity
        city.events.beforePersist = true
    }

    @EventListener
    void afterPersistSub(AfterPersistEvent e){
        City city = (City) e.entity
        city.name2 = "name2"
        city.events.afterPersist = true
        //we don't call persist, so it doesn't stuck in the infinite loop
        city.save()
    }

    @EventListener
    void beforeRemoveSub(BeforeRemoveEvent e){
        City city = (City) e.entity
        city.events.beforeRemove = true
    }

    @EventListener
    void afterRemoveSub(AfterRemoveEvent e){
        City city = (City) e.entity
        city.events.afterRemove = true
    }

}
