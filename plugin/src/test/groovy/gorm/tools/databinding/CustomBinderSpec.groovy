package gorm.tools.databinding

import gorm.tools.repository.*
import gorm.tools.testing.GormToolsTest
import grails.artefact.Artefact
import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import spock.lang.Specification

class CustomBinderSpec extends Specification implements GormToolsTest {

    void setupSpec() {
        defineBeans {
            customBinder(CustomBinder)
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

class CustomBinder implements MapBinder {

    @Override
    void bind(Object target, Map<String, Object> source, BindAction bindAction) {}

    @Override
    void bind(Object target, Map<String, Object> source) {}

    @Override
    void bindCreate(Object target, Map<String, Object> source) {}

    @Override
    void bindUpdate(Object target, Map<String, Object> source) {}
}

@Artefact("Repository")
class CityRepo extends DefaultGormRepo<City> {

    @Autowired
    @Qualifier("customBinder")
    CustomBinder mapBinder
}
