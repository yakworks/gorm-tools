/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.model

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * used as an helper for enum that is identity type. collects value for the get. map can also be used
 * should be used in conjunction with enumType: 'identity' in the mapping for hibernate/gorm.
 * Also used in entityMapBinding
 *
 * @param <E> the enum
 * @param <T> the id type
 *
 * @author Joshua Burnett (@basejump)
 *
 */
@SuppressWarnings(['PropertyName'])
@CompileStatic
trait IdEnum<E,T> {

    abstract T getId()
    // static abstract IdEnum[] values()

    static Map IdMap // = values().collectEntries { [(it.getId()): it]}

    @CompileDynamic
    static E get(T id) {
        if(!IdMap) IdMap = values().collectEntries { [(it.getId()): it]}
        return IdMap[id]
    }

}
