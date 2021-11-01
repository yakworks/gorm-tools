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
trait NameNum extends Named {

    String num

    static List qSearchIncludes = ['num', 'name'] // quick search includes
    static List picklistIncludes = ['id', 'num', 'name'] //for picklist

    // static Map includes = [
    //     qSearch: ['num', 'name'],
    //     picklist: ['id', 'num', 'name']
    // ]

    static constraintsMap = [
        name:[ description: 'The full name for this entity', nullable: false, blank: false, maxSize: 100],
        num:[ description: 'Unique alpha-numeric identifier for this entity', nullable: false, blank: false, maxSize: 50]
    ]

}
