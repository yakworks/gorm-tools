/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.taskify.domain

import gorm.tools.transform.IdEqualsHashCode
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class OrgExt {
    static belongsTo = [org:Org]

    Org orgParent
    String text1
    String text2


    static mapping = {
        id column: 'id', generator: 'foreign', params: [property: 'org']
        org insertable: false, updateable: false , column:'id'
        orgParent column: 'orgParentId'
    }
    static constraints = {
        orgParent nullable: true
        text1 nullable: true
        text2 nullable: true
    }
}
