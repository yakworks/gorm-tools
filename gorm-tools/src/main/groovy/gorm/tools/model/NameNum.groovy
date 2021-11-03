/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import groovy.transform.CompileStatic

/**
 * trait for a name num entity, common for Organizations(Customers etc..) and Contacts.
 */
@SuppressWarnings(['MethodName'])
@CompileStatic
trait NameNum extends NamedEntity {

    String num

    static Map includes = [
        qSearch: ['num', 'name'],
        picklist: ['id', 'num', 'name']
    ]

    static constraintsMap = [
        name:[ description: 'Full name for this entity', nullable: false, blank: false, maxSize: 100],
        num:[ description: 'Unique alpha-numeric identifier for this entity', nullable: false, blank: false, maxSize: 50]
    ]

}
