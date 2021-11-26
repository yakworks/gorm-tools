/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import gorm.tools.model.NamedEntity
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class Thing implements NamedEntity, GormRepoEntity<Thing, ThingRepo> {

    // address fields
    String name
    String country = "US"

    static Map includes = [
        qSearch: ['name', 'city'],
        stamp: ['id', 'name']
    ]

    static mapping = {
        id generator:'assigned'
    }

    static constraintsMap = [
        country:[ maxSize: 2 ]
    ]

    static Thing of(String name){
        new Thing(name: name)
    }
}
