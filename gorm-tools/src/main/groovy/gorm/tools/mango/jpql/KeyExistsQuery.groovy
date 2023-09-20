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
 * Simple performant way to check if a key value exists in the db for an entity.
 * This stores and caches a query that will look like this for psql with the default keyProp=id <br>
 *      select id from Entity where id=? limit ? (is sql server it would have "top 1" prefixed instead)
 *
 * The fastest way is NOT to do a count(*).
 * Note: The above could always end up being an Angry Monkey thing, so always confirm this as perhaps the DB

 * basically sets the limit to 1 with setMaxResults and checks if row count is at least 1
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@CompileStatic
class KeyExistsQuery<D> {
    Class<D> entityClass

    /** property/column to query */
    String keyName = "id"

    // org.hibernate.query.Query query
    HibernateGormStaticApi<D> staticApi

    KeyExistsQuery(Class<D> entityClass) {
        this.entityClass = entityClass
        staticApi = (HibernateGormStaticApi<D>) GormEnhancer.findStaticApi(entityClass)
    }

    static <D> KeyExistsQuery<D> of(Class<D> entityClass) {
        def inst = new KeyExistsQuery(entityClass)
        inst.staticApi = (HibernateGormStaticApi<D>) GormEnhancer.findStaticApi(entityClass)
        return inst
    }

    boolean exists(Object keyVal){
        String queryString = "select 1 from ${entityClass.name} where ${keyName} = :keyVal"
        //String query = "select ${idName} from ${entity.name} where id = :id"
        return (Boolean) staticApi.hibernateTemplate.execute { Session session ->
            Query q = (Query) session.createQuery(queryString)
            q.setReadOnly(true)
                .setMaxResults(1)
                .setParameter('keyVal', keyVal)

            return q.list().size() == 1
        }
    }

}
