/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.jpql

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import org.grails.datastore.gorm.GormEnhancer
import org.grails.orm.hibernate.HibernateGormStaticApi
import org.hibernate.Session
import org.hibernate.query.Query

/**
 * Simple performant way to check if a Composite Key value combination exists in the db for an entity.
 * see the single field KeyExistsQuery for more explanation
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@CompileStatic
class ComboKeyExistsQuery<D> {
    Class<D> entityClass

    /** property/column to query */
    List<String> keyNames
    /** cached query string */
    String queryString

    // org.hibernate.query.Query query
    HibernateGormStaticApi<D> staticApi

    ComboKeyExistsQuery(Class<D> entityClass) {
        this.entityClass = entityClass
        staticApi = (HibernateGormStaticApi<D>) GormEnhancer.findStaticApi(entityClass)
    }

    static <D> ComboKeyExistsQuery<D> of(Class<D> entityClass) {
        def inst = new ComboKeyExistsQuery(entityClass)
        inst.staticApi = (HibernateGormStaticApi<D>) GormEnhancer.findStaticApi(entityClass)
        return inst
    }

    boolean exists(Map<String,?> params){
        //key set should match
        if(params.keySet().toList() != keyNames)
            throw new IllegalArgumentException("params mismatch, params keys dont match the keyNames")
        if(!queryString) {
            queryString = "select 1 from ${entityClass.name} where"
            String whereClause = ""
            for(String keyName : keyNames){
                //if no whereClause then no AND
                String AND = whereClause ? "AND" : ""
                whereClause = "$whereClause $AND $keyName = :${paramValName(keyName)}"
            }
            queryString = "$queryString ${whereClause.trim()}"
        }

        return (Boolean) staticApi.hibernateTemplate.execute { Session session ->
            Query q = (Query) session.createQuery(queryString)
            q.setReadOnly(true).setMaxResults(1)

            params.each{ key, val ->
                String valKey = paramValName(key)
                q.setParameter(valKey, val)
            }

            return q.list().size() == 1
        }
    }

    String paramValName(String keyName){
        return "${keyName.replace(".","_")}Val"
    }

}
