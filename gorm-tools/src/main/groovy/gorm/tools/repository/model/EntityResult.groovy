/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic

import yakworks.api.ResultSupport
import yakworks.api.ResultTrait

/**
 * Entity result that allows the instance and some info about its status.
 * Used in upsert for example so we can know if the resulting entity was inserted or updated.
 * When Update is called then - statusCode = HttpStatus.OK.code
 * When Insert is called then - statusCode = HttpStatus.CREATED.code
 */
@CompileStatic
class EntityResult<D> implements ResultTrait<EntityResult<D>>, Serializable {
    //the entity this is for
    D entity

    EntityResult(D entity){
        this.entity = entity
        this.payload = entity
    }

    // ** BUILDERS STATIC OVERRIDES **
    static EntityResult OK(){ throw new UnsupportedOperationException("use generics") }
    static <D> EntityResult<D> of(D entity) {
        return new EntityResult(entity)
    }
    static <D> EntityResult<D> ofPayload(D entity) {
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
