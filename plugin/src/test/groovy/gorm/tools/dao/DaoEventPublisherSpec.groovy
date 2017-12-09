package gorm.tools.dao

import gorm.tools.TrxService
import gorm.tools.dao.events.DaoEventPublisher
import gorm.tools.dao.events.DaoEventType
import gorm.tools.testing.DaoDataTest
import grails.persistence.Entity
import spock.lang.Specification

class DaoEventPublisherSpec extends Specification implements DaoDataTest {

    void setup() {
        mockDomain(City)
    }

    Closure doWithSpring() {{ ->
        trxService(TrxService)
    }}

    DaoEventPublisher daoEventInvoker

    void testEventsFired() {
        given:
        //CityDao cityDao = City.dao
        Map params = [id:1, name: "test"]

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
        //CityDao cityDao = City.dao
        City city = new City()
        Map params = [name: "test"]

        when:
        daoEventInvoker.invokeEventMethod(City.dao, DaoEventType.BeforeUpdate.eventKey, city, params)

        then:
        city.region == "beforeUpdate"

        when:
        daoEventInvoker.invokeEventMethod(City.dao, DaoEventType.AfterUpdate.eventKey, city, params)

        then:
        city.region == "afterUpdate"

        when:
        daoEventInvoker.invokeEventMethod(City.dao, DaoEventType.BeforeRemove.eventKey, city, params)

        then:
        city.region == "beforeRemove"

        when:
        daoEventInvoker.invokeEventMethod(City.dao    , DaoEventType.AfterRemove.eventKey, city, params)

        then:
        city.region == "afterRemove"
    }
}


@Entity
class City {
    String name
    String region
}

class CityDao implements GormDao<City> {

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

