/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing


import gorm.tools.repository.model.UuidGormRepo
import gorm.tools.repository.model.UuidRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class UuidSample implements UuidRepoEntity<UuidSample> {
    UUID id
    String name

    static mapping = {
        id generator: "assigned"
    }
    static constraints = {
        name nullable: false
    }
}
