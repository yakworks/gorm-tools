/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import gorm.tools.repository.api.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class Project implements RepoEntity<Project> {

    String name
    String description
    boolean isActive
    Nested nested
    Date testDate

    static constraints = {
        name        nullable: false, example: 'project name'
        description nullable: true,  example: 'project description'
        nested nullable: false
        testDate nullable: false, example: "2017-01-01"
        isActive nullable: false, example: 'false'
    }
}
