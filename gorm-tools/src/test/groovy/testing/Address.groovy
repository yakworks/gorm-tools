/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import gorm.tools.repository.model.RepoEntity
import yakworks.commons.transform.IdEqualsHashCode
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class Address implements RepoEntity<Address>{
    String address
    AddyNested nested

    static constraints = {
        nested nullable: false
        address nullable: false
    }
}
