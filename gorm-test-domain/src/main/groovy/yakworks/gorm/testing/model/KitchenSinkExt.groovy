/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class KitchenSinkExt implements RepoEntity<KitchenSinkExt>{
    static belongsTo = [kitchenSink: KitchenSink]

    KitchenSink kitchenParent
    String text1
    String text2
    String textMax

    static mapping = {
        id column: 'id', generator: 'foreign', params: [property: 'kitchenSink']
        kitchenSink insertable: false, updateable: false , column:'id'
        kitchenParent column: 'custParentId'
    }
    static constraints = {
        kitchenParent nullable: true
        text1 nullable: true
        text2 nullable: true
        textMax maxSize: 2
    }
}
