/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import gorm.tools.databinding.BindAction
import yakworks.commons.map.Maps

/**
 * Gorm takes a Map of arguments such as validate, failOnError, etc.
 * This is a concrete object for those that that the Gorm-Tools repo utilizes.
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@ToString
@AutoClone
@CompileStatic
class PersistArgs {

    PersistArgs() { this([:])}

    /**
     * Should validate before save
     * default is true in gorm
     */
    Boolean validate

    /**
     * whether to drill into associations and validate those as well.
     * default is true in gorm
     */
    Boolean deepValidate

    /**
     * Throws exception on validation errors
     */
    Boolean failOnError = true

    /**
     * flush after persist
     */
    Boolean flush

    /**
     * if this is a known insert set to true to help it along
     */
    Boolean insert

    /**
     * create or update
     */
    BindAction bindAction

    /**
     * when calling create, set to true if there is an id in the data that should be used
     */
    Boolean bindId

    /**
     * The data used to bind during create or update
     * if it was a Create or Update method called then this is the data and gets passed into events
     */
    Map data

    /**
     * any extra params to pass through the repo methods
     */
    Map params = [:]


    static PersistArgs of(Map args = [:]){
        new PersistArgs(args)
    }

    /**
     * gets a new default instance
     */
    static PersistArgs 'new'(){
        PersistArgs.of()
    }

    /**
     * just a semantic variation of new to create a new.
     */
    static PersistArgs defaults(){
        PersistArgs.of()
    }

    /**
     * convert to Map to pass to gorm args. filters out nulls so only set properties will have keys set
     * @return the Map
     */
    Map asMap() {
        def mp = Maps.prune(this.properties) //prune out nulls
        mp.remove('class')
        return mp
    }

    public <T> T asType(Class<T> clazz) {
        if (Map.isAssignableFrom(clazz)) {
            return (T) asMap()
        }
        else {
            super.asType(clazz)
        }
    }


}
