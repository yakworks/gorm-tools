package gorm.tools.databinding

import gorm.tools.repository.*
import gorm.tools.testing.GormToolsTest
import grails.artefact.Artefact
import grails.databinding.DataBinder
import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class CustomBinderSpec extends Specification implements GormToolsTest {

    void setupSpec() {
        defineBeans {
            customBinder(CustomBinder, grailsApplication)
        }

        mockDomains(City)
    }

    void "verify binder is replaced"() {
        expect:
        City.repo.mapBinder instanceof CustomBinder
    }
}


@Artefact("Domain")
class City {
    String name
}

class CustomBinder extends EntityMapBinder {

}

@Artefact("Repository")
class CityRepo extends DefaultGormRepo<City> {

    @Autowired
    @Qualifier("customBinder")
    MapBinder mapBinder
}
