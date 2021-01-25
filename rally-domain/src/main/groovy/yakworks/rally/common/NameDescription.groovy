/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@SuppressWarnings(['MethodName'])
@CompileStatic
trait NameDescription {

    String name
    String description

    static List qSearchIncludes = ['name', 'description'] // quick search includes
    static List picklistIncludes = ['id', 'name'] //for picklist

    @CompileDynamic //ok, for gorm constraints
    static NameDescriptionConstraints(Object delegate) {
        def c = {
            description description: "the description for this entity",
                nullable: true, maxSize: 255
            name description: "the name of this entity",
                nullable: false, blank: false, maxSize: 50
        }
        c.delegate = delegate
        c()
    }

}
