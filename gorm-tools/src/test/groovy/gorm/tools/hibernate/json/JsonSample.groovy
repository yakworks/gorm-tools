/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.json

import com.vladmihalcea.hibernate.type.json.JsonType
import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.UuidRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import com.vladmihalcea.hibernate.type.json.JsonType

@Entity
// @TypeDef(name = "json", typeClass = JsonType.class)
@GrailsCompileStatic
class JsonSample implements UuidRepoEntity<JsonSample, GormRepo<JsonSample>> {
    UUID id
    String name

    @Type(type = 'com.vladmihalcea.hibernate.type.json.JsonType')
    Map json = [:]

    static mapping = {
        id generator: "uuid2"
        // json type: JsonType, sqlType: 'json'
    }
    static constraints = {
        name nullable: false
    }
}
