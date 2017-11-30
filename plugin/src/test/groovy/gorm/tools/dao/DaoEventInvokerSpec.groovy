package gorm.tools.dao

import gorm.tools.databinding.FastBinder
import gorm.tools.testing.DaoDataTest
import grails.artefact.Artefact
import grails.gorm.annotation.Entity
import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest
import spock.lang.IgnoreRest
import spock.lang.Specification

class DaoEventInvokerSpec extends Specification implements AutowiredTest, DaoDataTest {

	void setup() {
		mockDomain(City)
		mockDao(CityDao)
	}

	DaoEventInvoker daoEventInvoker

	void testEventsFired() {
		given:
		CityDao cityDao = City.dao
		Map params = [id:1, name: "test"]

		when:
		City city = cityDao.create(params)

		then:
		city != null
		city.region == "beforeCreate"

		when:
		city = cityDao.update(params)

		then:
		city != null
		city.region == "afterUpdate"
	}

	void testInvokeEvent() {
		given:
		CityDao cityDao = City.dao
		City city = new City()
		Map params = [name: "test"]

		when:
		daoEventInvoker.invokeEvent(cityDao, DaoEventType.BeforeUpdate, city, params)

		then:
		city.region == "beforeUpdate"

		when:
		daoEventInvoker.invokeEvent(cityDao, DaoEventType.AfterUpdate, city, params)

		then:
		city.region == "afterUpdate"

		when:
		daoEventInvoker.invokeEvent(cityDao, DaoEventType.BeforeRemove, city, params)

		then:
		city.region == "beforeRemove"

		when:
		daoEventInvoker.invokeEvent(cityDao, DaoEventType.AfterRemove, city, params)

		then:
		city.region == "afterRemove"
	}
}


@Entity
@Artefact("Domain")
class City {
	String name
	String region
}

@Artefact("Dao")
class CityDao extends DefaultGormDao<City> {

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

