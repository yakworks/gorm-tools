/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.json

import groovy.transform.CompileDynamic

import gorm.tools.hibernate.type.JsonType
import gorm.tools.repository.model.UuidGormRepo
import gorm.tools.repository.model.UuidRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

@Entity
@GrailsCompileStatic
class JsonSample implements UuidRepoEntity<JsonSample, UuidGormRepo<JsonSample>> {
    UUID id
    String name

    Map json = [:]
    List someList = []

    //json mapped to pogo
    Addy addy

    // static mapping = orm {
    //     id generator: "assigned"
    //     version false
    //     property("json", [type: JsonType, params: [clazz: Map.name] ])
    //     // property("someList", [type: JsonType, params: [clazz: ArrayList.name] ])
    //     property("someList", {
    //         column([name: "someList"])
    //         type = JsonType
    //         typeParams = [clazz: ArrayList.name]
    //     })
    // }
    @CompileDynamic
    static Closure getMapping(){ { ->
        id generator: "assigned"
        json type: JsonType, params: [type: Map]
        someList type: JsonType, params: [type: ArrayList]
        addy type: JsonType, params: [type: Addy]
    }}

    static constraints = {
        name nullable: false
    }
}
