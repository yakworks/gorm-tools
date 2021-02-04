/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.model


import groovy.transform.EqualsAndHashCode

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.common.NameDescription

@AuditStamp
@Entity
@EqualsAndHashCode(includes=["name", "entityName"])
@GrailsCompileStatic
class Tag implements NameDescription, RepoEntity<Tag>, Serializable {
    // static transients = ['entityNameList']
    String name
    String description

    //the domain entity this tag is valid for, null if is good for any taggable entity, can be comma seperated list
    String entityName
    //transiet to cache the list for isValidFor this is valid for if its multiple
    private List entityNameList

    static mapping = {
        cache true
    }

    static constraints = {
        description description: "the description for this tag",
            nullable: true, maxSize: 255
        name description: "the tag name",
            nullable: false, blank: false, maxSize: 50, unique: ["entityName"]
        entityName nullable: true
    }

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
