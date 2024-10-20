/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.json

import groovy.transform.CompileDynamic

import org.grails.datastore.mapping.config.MappingDefinition

import gorm.tools.hibernate.type.JsonType
import gorm.tools.repository.model.UuidGormRepo
import gorm.tools.repository.model.UuidRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

@Entity
@GrailsCompileStatic
class JsonSample implements UuidRepoEntity<JsonSample> {
    UUID id
    String name

    Map json = [:]
    List<Integer> someList = []

    //json mapped to pogo
    Addy addy

    static mapping = orm {
        id generator: "assigned"
        columns(
            json: property(type: JsonType, typeParams: [type: Map]),
            someList: property(type: JsonType, typeParams: [type: ArrayList]),
            addy: property(type: JsonType, typeParams: [type: Addy]),
        )
    }

    static constraints = {
        name nullable: false
    }
}
