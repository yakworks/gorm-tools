/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

// One to many, one kitchenSink has many SinkItems
@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class SinkItem implements RepoEntity<SinkItem>{
    static belongsTo = [kitchenSink: KitchenSink]

    String name

    static mapping = {
        kitchenSink column: 'kitchenSinkId'
    }
    static constraintsMap = [
        kitchenSink:[ nullable: false],
        name:[ nullable: false]
    ]
}
