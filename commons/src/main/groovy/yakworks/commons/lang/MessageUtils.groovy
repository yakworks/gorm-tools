/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import java.math.RoundingMode

import groovy.transform.CompileStatic

@CompileStatic
class MessageUtils {

    /**
     * Builds a msg key list from property path. See unit tests.
     * example1: will return ['customer.name', 'name'] when ('customer, 'name') is passed in as args.
     * or return ['ar.status.name', 'status.name', 'name'] when ('ar,'status.name') is passed in as args.
     */
    static List<String> labelKeysFromPath(String classProp, String key){
        List keys = [] as List<String>
        //first lookup match with classProp perpended, that wins
        keys << "${classProp}.${key}".toString()
        //next add the key itself
        keys << key

        List parts = key.split(/\./) as List<String>
        //remove first item since we already added full key
        parts.remove(0)
        //reverse it now to build keys from field up
        parts = parts.reverse()
        List<String> partKeys = []
        String partKey = ""
        for(String part: parts){
            partKey = partKey ? "${part}.${partKey}" : part
            partKeys << partKey
        }
        keys.addAll(partKeys.reverse())
        return keys
    }
}
