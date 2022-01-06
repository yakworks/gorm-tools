/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.databinding

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileStatic

import org.grails.databinding.converters.ConversionService
import org.grails.databinding.converters.DateConversionHelper
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import gorm.tools.repository.model.RepoEntity
import gorm.tools.testing.unit.DataRepoTest
import grails.databinding.converters.ValueConverter
import grails.persistence.Entity
import spock.lang.Specification
import yakworks.commons.lang.IsoDateUtil
import yakworks.commons.model.IdEnum
import yakworks.gorm.testing.model.KitchenSink

class PathKeyMapEntityMapBinderSpec extends Specification implements DataRepoTest {
    EntityMapBinder binder

    void setup() {
        binder = new EntityMapBinder()
    }

    Class[] getDomainClassesToMock() {
        [KitchenSink]
    }

    void "test bind using PathKeyMap"() {
        Map sub = [
            name2: "name2",
            inactive: "true",
            amount: "100.00",
            "sinkLink.name2": "sinkLink.name2",
            "thing.name" : "thing"
        ]

        when:
        PathKeyMap params = PathKeyMap.of(sub).init()
        KitchenSink sink = new KitchenSink()
        binder.bind(sink, params)

        then:
        sink.name2 == "name2"
        sink.inactive == true
        sink.amount == 100.00
        sink.sinkLink.name2 == "sinkLink.name2"
        sink.thing == null //this is not bindable
    }


}
