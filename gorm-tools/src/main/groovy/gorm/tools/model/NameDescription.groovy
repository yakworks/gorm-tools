/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import groovy.transform.CompileStatic

@SuppressWarnings(['MethodName'])
@CompileStatic
trait NameDescription {

    String name
    String description

    static List qSearchIncludes = ['name', 'description'] // quick search includes
    static List picklistIncludes = ['id', 'name'] //for picklist

    static constraintsMap = [
        name:[ description: 'The name for this entity', nullable: false, blank: false, maxSize: 50],
        description:[ description: 'The description for this entity', nullable: true, maxSize: 255]
    ]
}
