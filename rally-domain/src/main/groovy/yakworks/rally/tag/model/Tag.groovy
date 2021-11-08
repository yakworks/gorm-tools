/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.model


import groovy.transform.EqualsAndHashCode

import gorm.tools.audit.AuditStamp
import gorm.tools.model.NameCodeDescription
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@AuditStamp
@Entity
@EqualsAndHashCode(includes=["name", "entityName"])
@GrailsCompileStatic
class Tag implements NameCodeDescription, RepoEntity<Tag>, Serializable {
    // static transients = ['entityNameList']
    String name

    //the domain entity this tag is valid for, null if is good for any taggable entity, can be comma seperated list
    String entityName
    //transiet to cache the list for isValidFor this is valid for if its multiple
    private List entityNameList

    static mapping = {
        cache true
    }

    static constraintsMap = [
        description:[ description: "The description for this tag",
            nullable: true, maxSize: 255],
        name:[ description: "The tag name",
            nullable: false, maxSize: 50, unique: ["entityName"] ],
        entityName:[description: "The entity this tag can be applied to. May be a comma sep list of entity names", nullable: true],
    ]

    static Tag getByName(String theName, String theEntityName){
        Tag.where { name == theName && entityName == theEntityName }.get()
    }

    boolean isValidFor(String entName){
        if(entityNameList == null){ //initialize
            String entityNameTrim = entityName?.trim()
            entityNameList = entityNameTrim ? [entityNameTrim] : []  //empty list so we know its init
            if(entityNameTrim?.contains(',')) entityNameList = entityNameTrim.split(/\s*,\s*/) as List
        }
        return entityNameList.isEmpty() || entityNameList.contains(entName)
    }
}
