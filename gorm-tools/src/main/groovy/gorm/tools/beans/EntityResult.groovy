/*
* Copyright 2021 original authors
* SPDX-License-Identifier: Apache-2.0
*/
package gorm.tools.beans

import groovy.transform.CompileStatic

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.api.Result
import yakworks.api.ResultSupport
import yakworks.api.ResultTrait

/**
 * Entity result that allows the instance and some info about its status.
 * Used in upsert for example so we can know if the resulting entity was inserted or updated.
 */
@CompileStatic
class EntityResult<D> implements ResultTrait<EntityResult<D>>, Serializable {
    //the entity this is for
    D entity

    /**
     * New result
     * @param isSynchronized defaults to true to create the data list as synchronizedList
     */
    EntityResult(D entity){
        this.entity = entity
        this.payload = entity
    }

    // ** BUILDERS STATIC OVERRIDES **
    static EntityResult OK(){ throw new UnsupportedOperationException("use generics") }
    static EntityResult<D> of(D entity) {
        return new EntityResult(entity)
    }
    static EntityResult<D> ofPayload(D entity) {
        return new EntityResult(entity)
    }

    EntityResult<D> ok(boolean v){
        ok = v
        return this
    }

    /**
     * converts to Map, helpfull for to json and can be overriden on concrete impls
     */
    @Override
    Map<String, Object> asMap(){
        Map<String, Object> hmap = ResultSupport.toMap(this);
        return hmap;
    }

}
