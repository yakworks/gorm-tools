/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data

import groovy.transform.CompileStatic

@CompileStatic
class ProblemUtils {

    /**
     * add arguments for things in entity
     * - add name as class.simpleName
     * - add id if it has one
     * - add stamp if it has one
     * returns null if the msg
     */
    static Map addCommonArgs(Map args, Object entity){
        if(args == null) return args
        args.putIfAbsent('name', entity.class.simpleName)
        if (entity.hasProperty('id') && entity['id'])
            args.putIfAbsent('id', entity['id'])

        if (entity.hasProperty('stamp'))
            args.putIfAbsent('stamp', entity['stamp'])
    }
}
