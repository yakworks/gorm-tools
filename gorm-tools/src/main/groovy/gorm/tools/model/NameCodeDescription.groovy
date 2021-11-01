/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import groovy.transform.CompileStatic

import gorm.tools.repository.RepoUtil

@SuppressWarnings(['MethodName'])
@CompileStatic
trait NameCodeDescription<D> extends NameCode<D> {

    String description

    static List qSearchIncludes = ['name', 'code', 'description'] // quick search includes

    static constraintsMap = [
        description:[ description: 'The description for this entity', nullable: true, maxSize: 255]
    ]

}
