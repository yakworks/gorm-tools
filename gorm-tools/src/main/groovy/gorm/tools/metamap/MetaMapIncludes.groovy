/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j

import yakworks.commons.lang.NameUtils

/**
 * Includes tree for root entity and nested association properties
 */
@Slf4j
@EqualsAndHashCode(includes=["className", "props"], useCanEqual=false) //because its used as cache key
@CompileStatic
class MetaMapIncludes {
    String className
    //value will be null if normal prop, if association then will have another nested MetaMapIncludes
    Map props = [:] as Map<String, MetaMapIncludes>
    Set<String> excludeFields
    //used for openApi to add the schema into it.
    Object schema

    MetaMapIncludes(){
    }

    MetaMapIncludes(String className){
        this.className = className
    }

    MetaMapIncludes(Map<String, MetaMapIncludes> props){
        this.props = props
    }


    MetaMapIncludes(String className, Set<String> fields, Set<String> excludeFields){
        this.className = className
        addBlacklist(excludeFields)
    }

    static MetaMapIncludes of(List<String> fields){
        def mmi = new MetaMapIncludes()
        fields.each { mmi.props[it] = null }
        return mmi
    }

    /**
     * Filters the props to only the ones that are association and have a nested includes
     */
    Map<String, MetaMapIncludes> getNestedIncludes(){
        return props.findAll { it.value != null} as Map<String, MetaMapIncludes>
    }

    /**
     * gets the class name with out prefix sowe can lookup the openapi schema
     */
    String getShortClassName(){
        return NameUtils.getShortName(className)
    }

    void addBlacklist(Set<String> excludeFields) {
        this.excludeFields = excludeFields
        this.props.keySet().removeAll(excludeFields)
    }

    /**
     * merges another MetaMapIncludes fields and nested includes
     */
    void merge(MetaMapIncludes toMerge) {
        this.props.putAll(toMerge.props)
        // if(toMerge.nestedIncludes) this.nestedIncludes.putAll(toMerge.nestedIncludes)
    }
}
