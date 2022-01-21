/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.query.Query
import org.grails.orm.hibernate.AbstractHibernateSession
import org.grails.orm.hibernate.GrailsHibernateTemplate
import org.grails.orm.hibernate.query.HibernateQuery
import org.hibernate.Criteria

/**
 * Extends to overrides the myriad of issues with how the DetachedCriteria was translating to the
 * the HibernateQuery. Mostly around the nested and aliases. For example ordering by something like
 * foo.bar.baz was not working at all. Also made it so we can take advantage of being able to specify the
 * ordering for nulls so we can keep postgress consistent with mysql and mssql etc..
 *
 */
@CompileStatic
class HibernateMangoQuery extends HibernateQuery {

    HibernateMangoQuery(Criteria criteria, AbstractHibernateSession session, PersistentEntity entity) {
        super(criteria, session, entity);
    }

    static Query createQuery(AbstractHibernateSession hibernateSession, PersistentEntity entity, String alias) {
        Class type = entity.javaClass
        GrailsHibernateTemplate hibernateTemplate = (GrailsHibernateTemplate)hibernateSession.getNativeInterface()
        org.hibernate.Session currentSession = hibernateTemplate.getSessionFactory().getCurrentSession()
        final Criteria criteria = alias != null ? currentSession.createCriteria(type, alias) : currentSession.createCriteria(type);
        hibernateTemplate.applySettings(criteria)
        return new HibernateMangoQuery(criteria, hibernateSession, entity)
    }

    @Override
    Query order(Order order) {
        if (order != null) {
            orderBy.add(order);
        }

        String property = order.getProperty()

        int i = property.indexOf('.')
        int iLast = property.lastIndexOf('.')
        if(i > -1) {

            // String sortHead = property.substring(0,i);
            // String sortTail = property.substring(i + 1);
            String sortHead = property.substring(0, iLast)
            String sortTail = property.substring(iLast + 1)

            if(createdAssociationPaths.containsKey(sortHead)) {
                CriteriaAndAlias criteriaAndAlias = createdAssociationPaths.get(sortHead)
                String alias = getCriteriaAlias(criteriaAndAlias)
                String aliasedProp = "${alias}.${sortTail}"
                // Criteria criteria = getCriteriaOnAlias(criteriaAndAlias)
                org.hibernate.criterion.Order hibernateOrder = order.getDirection() == Order.Direction.ASC ?
                    org.hibernate.criterion.Order.asc(aliasedProp) : org.hibernate.criterion.Order.desc(aliasedProp)
                getHibernateCriteria().addOrder(order.isIgnoreCase() ? hibernateOrder.ignoreCase() : hibernateOrder)
                return this
            }
            else {
                return super.order(order)
            }
        }
        else {
            return super.order(order)
        }
    }

    @CompileDynamic //so we can access protected
    Criteria getCriteriaOnAlias(CriteriaAndAlias criteriaAndAlias){
        return criteriaAndAlias.criteria
    }

    @CompileDynamic //so we can access protected
    String getCriteriaAlias(CriteriaAndAlias criteriaAndAlias){
        return criteriaAndAlias.alias
    }

}
