/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.meta

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.mapping.reflect.ClassUtils

import yakworks.commons.map.Maps
import yakworks.commons.model.TotalCount

/**
 * A list wrapper that will wrap object in EntityMap on a get()
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1.12
 */
@SuppressWarnings(["CompileStatic", "ExplicitCallToEqualsMethod"])
@CompileStatic
class MetaMapList extends AbstractList<MetaMap> implements TotalCount  {

    protected List resultList
    protected MetaMapIncludes includeMap

    MetaMapList(List resultList) {
        this.resultList = resultList
    }

    MetaMapList(List resultList, MetaMapIncludes includeMap) {
        this.resultList = resultList
        this.includeMap = includeMap
    }

    @Override
    @CompileDynamic //not a performance hit
    int getTotalCount() {
        if(ClassUtils.isPresent('grails.gorm.PagedResultList') || resultList instanceof TotalCount) {
            return resultList.totalCount
        }
        else {
            return resultList.size()
        }
    }

    /**
     * wraps the item in a
     */
    @Override
    MetaMap get(int i) {
        def origObj = resultList.get(i)
        def eb = new MetaMap(origObj, includeMap)
        return eb
    }

    @Override
    int size() {
        return resultList.size()
    }

    @Override
    boolean equals(Object o) {
        return resultList.equals(o)
    }

    @Override
    int hashCode() {
        return resultList.hashCode()
    }

    // private void writeObject(ObjectOutputStream out) throws IOException {
    //
    //     // find the total count if it hasn't been done yet so when this is deserialized
    //     // the null GrailsHibernateTemplate won't be an issue
    //     getTotalCount();
    //
    //     out.defaultWriteObject();
    // }

    @Override
    Object clone() {
        return Maps.clone(this as Collection<Map>)
    }
}
