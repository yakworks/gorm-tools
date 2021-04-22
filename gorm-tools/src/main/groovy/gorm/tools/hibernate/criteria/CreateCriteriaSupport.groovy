/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.criteria

import groovy.transform.CompileDynamic

import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.grails.orm.hibernate.datasource.MultipleDataSourceSupport
import org.hibernate.SessionFactory

import grails.util.Holders

/**
 * Deprecated USE QUERY or WHERE INSTEAD
 *
 * Creates a improved  criteria builder instance
 * make it easier to build criteria with domain bean paths
 * allows
 * eq('invoice.customer.name', 'foo')
 *
 * instead of
 * invoice {
 *      customer {
 *          eq(name)
 *      }
 *  }
 * simliar with eq, like and in
 */
@CompileDynamic
trait CreateCriteriaSupport {

    /**
     * Deprecated USE QUERY or WHERE INSTEAD
     *
     * Creates a improved  criteria builder instance
     * make it easier to build criteria with domain bean paths
     * allows
     * eq('invoice.customer.name', 'foo')
     *
     * instead of
     * invoice {
     *      customer {
     *          eq(name)
     *      }
     *  }
     * simliar with eq, like and in
     */
    @Deprecated
    static BuildableCriteria createCriteria() {
        BuildableCriteria builder
        //TODO temp hack, to prevent unit tests failing
        String datasourceName = MultipleDataSourceSupport.getDefaultDataSource(currentGormStaticApi().persistentEntity)
        boolean isDefault = (datasourceName == ConnectionSource.DEFAULT)
        String suffix = isDefault ? '' : '_' + datasourceName
        try {
            builder = new GormHibernateCriteriaBuilder(this, Holders.applicationContext.getBean("sessionFactory$suffix".toString(), SessionFactory))
            builder.conversionService = currentGormStaticApi().datastore.mappingContext.conversionService
        } catch(IllegalStateException){
            builder = currentGormStaticApi().createCriteria()
        }

        return builder
    }
}
