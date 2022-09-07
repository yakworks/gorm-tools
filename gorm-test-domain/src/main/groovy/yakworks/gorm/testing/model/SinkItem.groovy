/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import gorm.tools.model.NamedEntity
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

// One to many, one kitchenSink has many SinkItems
@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class SinkItem implements NamedEntity, RepoEntity<SinkItem>{
    static belongsTo = [kitchenSink: KitchenSink]

    static mapping = {
        kitchenSink column: 'kitchenSinkId'
    }
    static constraintsMap = [
        kitchenSink:[ nullable: false]
    ]

    static List<SinkItem> listByKitchenSink(KitchenSink sink){
        SinkItem.where { kitchenSink == sink }.list()
    }
}
