/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.model

import gorm.tools.model.NamedEntity
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

// One to One
@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class SinkExt implements NamedEntity, RepoEntity<SinkExt>{
    static belongsTo = [KitchenSink]

    KitchenSink kitchenParent
    String name
    String textMax
    Thing thing

    static mapping = {
        id generator: 'assigned'
        kitchenParent column: 'kitchenParentId'
    }

    static constraints = {
        kitchenParent nullable: true
        name nullable: false
        textMax maxSize: 2, nullable: true
    }

    void setKitchenSink(KitchenSink val){ id = val.id}

}
