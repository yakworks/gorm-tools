/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@SuppressWarnings(['MethodName'])
@CompileStatic
trait NameNum {

    String num
    String name

    static List qSearchIncludes = ['num', 'name'] // quick search includes
    static List picklistIncludes = ['id', 'num', 'name'] //for picklist

    @CompileDynamic //ok, for gorm constraints
    static NameNumConstraints(Object delegate, Map overrideProps = null) {
        def numDefault = [description: "Unique alpha-numeric identifier for this entity",
                          nullable: false, blank: false, maxSize: 50]

        def nameDefault = [description: "The full name for this entity",
                           nullable: false, blank: false, maxSize: 100]

        //use default, but allow to override
        if(overrideProps){
            if(overrideProps.num) numDefault << overrideProps.num as Map
            if(overrideProps.name) nameDefault << overrideProps.name as Map
        }

        Closure c = {
            num numDefault
            name nameDefault
        }
        c.delegate = delegate
        c()
    }

}
