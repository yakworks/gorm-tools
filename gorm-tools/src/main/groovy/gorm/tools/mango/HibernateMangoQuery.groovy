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
import org.grails.orm.hibernate.query.AbstractHibernateQuery
import org.grails.orm.hibernate.query.HibernateProjectionAdapter
import org.grails.orm.hibernate.query.HibernateQuery
import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Projections

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

    /**
     * creates and instance of this. Copied in from the Session as is not easily overriden there
     */
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

    /**
     * List but uses the CriteriaSpecification.ALIAS_TO_ENTITY_MAP for projection queries
     */
    List mapList() {
        def crit = getHibernateCriteria()
        if(crit == null) throw new IllegalStateException("Cannot execute query using a detached criteria instance");

        int projectionLength = 0;
        def projs = projections() as HibernateAliasProjectionList
        if (projs != null) {
            org.hibernate.criterion.ProjectionList projectionList = projs.getHibernateProjectionList();
            projectionLength = projectionList.getLength();

            if(projectionLength > 0) {
                crit.setProjection(projectionList);
            }
        }
        crit.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);

        applyDefaultSortOrderAndCaching()
        applyFetchStrategies()

        // return listForCriteria()
        return crit.list()
    }

    org.hibernate.criterion.ProjectionList hibernateProjections(){
        def projs = projections() as HibernateAliasProjectionList
        return projs.getHibernateProjectionList()
    }

    Criteria initProjections() {
        def crit = getHibernateCriteria()

        int projectionLength = 0;
        def projs = projections() as HibernateAliasProjectionList
        if (projs != null) {
            org.hibernate.criterion.ProjectionList projectionList = projs.getHibernateProjectionList();
            projectionLength = projectionList.getLength();

            if(projectionLength > 0) {
                crit.setProjection(projectionList);
            }
        }
        crit.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);

        // applyDefaultSortOrderAndCaching()
        // applyFetchStrategies()

        // return listForCriteria()
        return crit
    }

    HibernateAliasProjectionList hibernateAliasProjectionList
    @Override
    @CompileDynamic
    ProjectionList projections() {
        if (hibernateAliasProjectionList == null) {
            hibernateAliasProjectionList = new HibernateAliasProjectionList()
        }
        return hibernateAliasProjectionList;
    }

    class HibernateAliasProjectionList extends Query.ProjectionList {

        org.hibernate.criterion.ProjectionList _projectionList = Projections.projectionList();
        private boolean rowCount = false;

        public boolean isRowCount() {
            return rowCount;
        }

        public org.hibernate.criterion.ProjectionList getHibernateProjectionList() {
            return _projectionList;
        }

        @Override
        public boolean isEmpty() {
            return _projectionList.getLength() == 0;
        }


        // @Override
        // public Query.ProjectionList add(Query.Projection p) {
        //     _projectionList.add(new HibernateProjectionAdapter(p).toHibernateProjection());
        //     return this;
        // }

        @Override
        Query.ProjectionList add(Query.Projection p) {
            String propName = (p as Query.PropertyProjection).propertyName
            def hibProj = new HibernateProjectionAdapter(p).toHibernateProjection()

            _projectionList.add(hibProj, propName.replace('.', '_'))
            // _projectionList.add(hibProj, propName)
            return this
        }

        @Override
        public org.grails.datastore.mapping.query.api.ProjectionList countDistinct(String property) {
            _projectionList.add(Projections.countDistinct(calculatePropertyName(property)));
            return this;
        }

        @Override
        public org.grails.datastore.mapping.query.api.ProjectionList distinct(String property) {
            _projectionList.add(Projections.distinct(Projections.property(calculatePropertyName(property))));
            return this;
        }

        @Override
        public org.grails.datastore.mapping.query.api.ProjectionList rowCount() {
            _projectionList.add(Projections.rowCount());
            this.rowCount = true;
            return this;
        }

        @Override
        public Query.ProjectionList id() {
            _projectionList.add(Projections.id());
            return this;
        }

        @Override
        public Query.ProjectionList count() {
            _projectionList.add(Projections.rowCount());
            this.rowCount = true;
            return this;
        }

        @Override
        public Query.ProjectionList property(String name) {
            _projectionList.add(Projections.property(calculatePropertyName(name)));
            return this;
        }

        @Override
        public Query.ProjectionList sum(String name) {
            _projectionList.add(Projections.sum(calculatePropertyName(name)));
            return this;
        }

        @Override
        public Query.ProjectionList min(String name) {
            _projectionList.add(Projections.min(calculatePropertyName(name)));
            return this;
        }

        @Override
        public Query.ProjectionList max(String name) {
            _projectionList.add(Projections.max(calculatePropertyName(name)));
            return this;
        }

        @Override
        public Query.ProjectionList avg(String name) {
            _projectionList.add(Projections.avg(calculatePropertyName(name)));
            return this;
        }

        @Override
        public Query.ProjectionList distinct() {
            if(hibernateCriteria != null)
                hibernateCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            else if(detachedCriteria != null)
                detachedCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            return this;
        }
    }


}
