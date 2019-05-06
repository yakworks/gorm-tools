/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.databinding

import gorm.tools.repository.*
import gorm.tools.testing.unit.GormToolsTest
import grails.artefact.Artefact
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
