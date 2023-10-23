/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import groovy.transform.CompileStatic

@SuppressWarnings(['MethodName'])
@CompileStatic
trait NameDescription extends NamedEntity {

    String description

    static Map includes = [
        qSearch: ['name', 'description'],
        stamp: ['id', 'name'] //picklist or minimal for joins
    ]

    static constraintsMap = [
        description:[ d: 'The description for this entity', nullable: true, maxSize: 255]
    ]
}
