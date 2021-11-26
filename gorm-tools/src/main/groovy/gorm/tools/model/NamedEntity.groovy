/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import groovy.transform.CompileStatic

import yakworks.commons.model.Named

/**
 * trait for a name num entity, common for Organizations(Customers etc..) and Contacts.
 */
@SuppressWarnings(['MethodName'])
@CompileStatic
trait NamedEntity extends Named {

    String name

    static Map includes = [
        qSearch: ['name'],
        stamp: ['id', 'name']  //picklist or minimal for joins
    ]

    static constraintsMap = [
        name:[ d: 'The name for this entity', nullable: false, maxSize: 50 ],
    ]

}
