/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.taskify.domain

import groovy.transform.CompileStatic

import gorm.tools.traits.IdEnum
import grails.util.GrailsNameUtils

@CompileStatic
enum OrgType implements IdEnum<OrgType, Long> {
    Customer(1),
    CustAccount(2),
    Branch(3),

    final Long id

    OrgType(Long id) {
        this.id = id
    }

    /**
     * get the property name for this, will follow normal java bean prop names
     */
    String getPropertyName(){
        GrailsNameUtils.getPropertyName(name())
    }

    /**
     * get the property name with Id appended for the field name
     */
    String getIdFieldName(){
        getPropertyName() + "Id"
    }

    /**
     * case insensitive find
     */
    static OrgType findByName (String key ){
        return values().find { it.name().toLowerCase() == key.toLowerCase()}
    }
}
