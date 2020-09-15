/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.traits

import groovy.transform.CompileStatic

/**
 * used as an helper for enum that is identity type. collects value for the get. map can also be used
 * should be used in conjunction with enumType: 'identity' in the mapping.
 * Also used in entityMapBinding
 *
 * @param <E> the enum
 * @param <T> the id type
 */
@CompileStatic
trait IdEnum<E,T> {

    abstract T getId()

    static final Map IdMap = values().collectEntries { [(it.id): it]}

    static E get(T id) {
        return IdMap[id]
    }

    static abstract IdEnum[] values()

}
