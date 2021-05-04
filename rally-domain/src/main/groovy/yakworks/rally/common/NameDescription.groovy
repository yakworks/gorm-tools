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
    static NameDescriptionConstraints(Object delegate, Map overrideProps = null) {
        def descDefault = [description: "The description for this entity",
                          nullable: true, maxSize: 255]

        def nameDefault = [description: "The name for this entity",
                           nullable: false, blank: false, maxSize: 50]

        //use default, but allow to override
        if(overrideProps){
            if(overrideProps.description) descDefault << overrideProps.description as Map
            if(overrideProps.name) nameDefault << overrideProps.name as Map
        }

        Closure c = {
            description descDefault
            name nameDefault
        }
        c.delegate = delegate
        c()
    }

}
