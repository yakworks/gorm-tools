/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify.domain

// import org.grails.datastore.gorm.GormEntity

import grails.compiler.GrailsCompileStatic
import grails.gorm.annotation.Entity

@Entity
@GrailsCompileStatic
class OrgType {
    String name

    static constraints = {
        name blank: false, nullable: false
    }
}
