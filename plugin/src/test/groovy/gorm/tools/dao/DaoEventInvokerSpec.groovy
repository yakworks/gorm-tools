package gorm.tools.dao

import gorm.tools.dao.events.DaoEventInvoker
import gorm.tools.dao.events.DaoEventType
import gorm.tools.testing.DaoDataTest
import grails.persistence.Entity
import spock.lang.Specification

class DaoEventInvokerSpec extends Specification implements DaoDataTest {

    void setup() {
        mockDomain(City)
    }

    DaoEventInvoker daoEventInvoker

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
        daoEventInvoker.invokeEvent(City.dao, DaoEventType.BeforeUpdate, city, params)

        then:
        city.region == "beforeUpdate"

        when:
        daoEventInvoker.invokeEvent(City.dao, DaoEventType.AfterUpdate, city, params)

        then:
        city.region == "afterUpdate"

        when:
        daoEventInvoker.invokeEvent(City.dao, DaoEventType.BeforeRemove, city, params)

        then:
        city.region == "beforeRemove"

        when:
        daoEventInvoker.invokeEvent(City.dao    , DaoEventType.AfterRemove, city, params)

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

