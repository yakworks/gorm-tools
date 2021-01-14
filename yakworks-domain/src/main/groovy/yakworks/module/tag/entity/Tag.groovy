/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.module.tag.entity

import groovy.transform.EqualsAndHashCode

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.module.common.NameDescription

@AuditStamp
@Entity
@EqualsAndHashCode(includes=["name", "entityName"])
@GrailsCompileStatic
class Tag implements NameDescription, RepoEntity<Tag>, Serializable {
    String name
    String description
    //XXX remove fieldName
    String fieldName //the column/field this is valid for, null if a table tag or global tag
    String entityName //the table/domain this tag is valid for, null if is good for any taggable entity

    static mapping = {
        cache true
    }

    static constraints = {
        description description: "the description for this tag",
            nullable: true, maxSize: 255
        name description: "the tag name",
            nullable: false, blank: false, maxSize: 50, unique: ["fieldName", "entityName"]
        fieldName nullable: true
        entityName nullable: true
    }

    static Tag getByName(String theName, String theEntityName){
        Tag.where { name == theName && entityName == theEntityName }.get()
    }
}
