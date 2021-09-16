/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import groovy.transform.CompileStatic

/**
 * for GormEntity this marks it for a lookup static method, like a `get`,
 * that can get an eintity by other key fields other than just id
 */
@CompileStatic
trait Lookupable<D> {

    /**
     * lookup and get an entity based on keys in the data map
     * @param data the map with te other key data
     * @return the found entity or null if nothing was found
     */
    static D lookup(Map data){
        throw new UnsupportedOperationException("This method must be implemented in the class that has this trait")
    }
}
