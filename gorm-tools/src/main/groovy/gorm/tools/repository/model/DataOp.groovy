/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic

import yakworks.commons.lang.EnumUtils

/**
 * valid op values for binding associations
 * based loosely on http://jsonpatch.com/
 */
@CompileStatic
enum DataOp {
    add, update, upsert, remove, replace, copy, move

    /**
     * case insensitive find
     */
    static DataOp get(Object key){
        if(!key) return null

        return EnumUtils.getEnum(DataOp, key.toString().toLowerCase())
    }
}
