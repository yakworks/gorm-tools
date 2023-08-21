/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.hibernate

import java.lang.reflect.Field
import javax.persistence.criteria.JoinType

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.finders.DynamicFinder
import org.grails.datastore.gorm.query.criteria.DetachedAssociationCriteria
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.QueryableCriteria
import org.grails.datastore.mapping.query.event.PostQueryEvent
import org.grails.datastore.mapping.query.event.PreQueryEvent
import org.grails.orm.hibernate.AbstractHibernateSession
import org.grails.orm.hibernate.GrailsHibernateTemplate
import org.grails.orm.hibernate.HibernateSession
import org.grails.orm.hibernate.cfg.AbstractGrailsDomainBinder
import org.grails.orm.hibernate.cfg.Mapping
import org.grails.orm.hibernate.query.AbstractHibernateCriterionAdapter
import org.grails.orm.hibernate.query.AbstractHibernateQuery
import org.grails.orm.hibernate.query.HibernateCriterionAdapter
import org.grails.orm.hibernate.query.HibernateProjectionAdapter
import org.hibernate.Criteria
import org.hibernate.NonUniqueResultException
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Projections
import org.hibernate.dialect.Dialect
import org.hibernate.dialect.function.SQLFunction
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.internal.CriteriaImpl
import org.hibernate.persister.entity.PropertyMapping
import org.hibernate.type.BasicType
import org.hibernate.type.TypeResolver
import org.springframework.context.ApplicationEventPublisher

import grails.orm.HibernateCriteriaBuilder
import grails.orm.RlikeExpression

/**
 * Replaces org.grails.orm.hibernate.query.HibernateQuery since there was a myriad of issues with the DetachedCriteria
 *
 * Enhancments over what HibernateQuery was not doing properly
 * - auto setup of nested and aliases
 * - For example ordering by something like foo.bar.baz will work properly
 * - take advantage of being able to specify the ordering for nulls so we can keep postgress consistent with mysql and mssql etc..
 * - working on adding json querying.
 *
 * many of the overrides are here so we can use our HibernateAliasProjectionList
 */
@CompileStatic
class HibernateMangoQuery extends AbstractHibernateQuery  {

    public static final HibernateCriterionAdapter HIBERNATE_CRITERION_ADAPTER = new HibernateCriterionAdapter();

    HibernateAliasProjectionList hibernateAliasProjectionList

    Field hasJoinsField

    HibernateMangoQuery(Criteria criteria, AbstractHibernateSession session, PersistentEntity entity) {
        super(criteria, session, entity)
        //make hasJoins acccesible
        hasJoinsField = AbstractHibernateQuery.getDeclaredField("hasJoins")
        hasJoinsField.setAccessible(true)
    }


    /**
     * creates and instance of this. Copied in from the Session as is not easily overriden there
     */
    static Query createQuery(AbstractHibernateSession hibernateSession, PersistentEntity entity, String alias) {
        Class type = entity.javaClass
        GrailsHibernateTemplate hibernateTemplate = (GrailsHibernateTemplate)hibernateSession.getNativeInterface()
        Session currentSession = hibernateTemplate.getSessionFactory().getCurrentSession()
        final Criteria criteria = alias != null ? currentSession.createCriteria(type, alias) : currentSession.createCriteria(type);
        hibernateTemplate.applySettings(criteria)
        return new HibernateMangoQuery(criteria, hibernateSession, entity)
    }

    @CompileDynamic
    Criteria getHibernateCriteria() {
        return super.@criteria
    }

    @Override //implements abstract
    protected AbstractHibernateCriterionAdapter createHibernateCriterionAdapter() {
        return HIBERNATE_CRITERION_ADAPTER
    }

    @Override //implements abstract
    protected org.hibernate.criterion.Criterion createRlikeExpression(String propertyName, String value) {
        return new RlikeExpression(propertyName, value);
    }

    @Override //implements abstract
    protected void setDetachedCriteriaValue(QueryableCriteria value, PropertyCriterion pc) {
        DetachedCriteria hibernateDetachedCriteria = HibernateCriteriaBuilder.getHibernateDetachedCriteria(this, value);
        pc.setValue(hibernateDetachedCriteria);
    }

    @Override //implements abstract
    protected String render(BasicType basic, List<String> columns, SessionFactory sessionFactory, SQLFunction sqlFunction) {
        return sqlFunction.render(basic, columns, (SessionFactoryImplementor) sessionFactory);
    }

    @Override //implements abstract
    protected PropertyMapping getEntityPersister(String name, SessionFactory sessionFactory) {
        return (PropertyMapping) ((SessionFactoryImplementor) sessionFactory).getEntityPersister(name);
    }

    @Override
    @Deprecated
    protected TypeResolver getTypeResolver(SessionFactory sessionFactory) {
        return ((SessionFactoryImplementor) sessionFactory).getTypeResolver();
    }

    @Override
    @Deprecated
    protected Dialect getDialect(SessionFactory sessionFactory) {
        return ((SessionFactoryImplementor) sessionFactory).getDialect();
    }

    @Override //overrides to fix
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

    @Override
    protected CriteriaAndAlias getCriteriaAndAlias(DetachedAssociationCriteria associationCriteria) {
        String associationPath = associationCriteria.getAssociationPath();
        String alias = associationCriteria.getAlias();

        if(associationPath == null) {
            associationPath = associationCriteria.getAssociation().getName();
        }
        return getOrCreateAlias(associationPath, alias);
    }

    @Override
    protected CriteriaAndAlias getOrCreateAlias(String associationName, String alias) {
        CriteriaAndAlias subCriteria = null;
        String associationPath = getAssociationPath(associationName);
        Criteria parentCriteria = getHibernateCriteria();
        if(alias == null) {
            alias = generateAlias(associationName);
        }
        // else {
        //     //CriteriaAndAlias criteriaAndAlias = createdAssociationPaths.get(associationPath);
        //     CriteriaAndAlias criteriaAndAlias = createdAssociationPaths.get(alias);
        //     if(criteriaAndAlias != null) {
        //         parentCriteria = getCriteriaOnAlias(criteriaAndAlias);
        //         if(parentCriteria != null) {
        //
        //             alias = associationName + '_' + alias;
        //             associationPath = getCriteriaAndAliasAssociationPath(criteriaAndAlias) + '.' + associationPath;
        //         }
        //     }
        // }

        if (createdAssociationPaths.containsKey(associationPath)) { //this was using associationName as the key
            subCriteria = createdAssociationPaths.get(associationPath);
        }
        else {
            JoinType joinType = joinTypes.get(associationName);
            if(parentCriteria != null) {
                Criteria sc = parentCriteria.createAlias(associationPath, alias, resolveJoinType(joinType));
                //nasty hack for groovy and inner classes, need to pass in the instance to newInstance first which is "this"
                subCriteria = AbstractHibernateQuery.CriteriaAndAlias.newInstance(this, sc, alias, associationPath)
            }
            else if(detachedCriteria != null) {
                DetachedCriteria sc = detachedCriteria.createAlias(associationPath, alias, resolveJoinType(joinType));
                subCriteria = AbstractHibernateQuery.CriteriaAndAlias.newInstance(this, sc, alias, associationPath)
            }
            if(subCriteria != null) {

                createdAssociationPaths.put(associationPath, subCriteria);
                createdAssociationPaths.put(alias, subCriteria);
            }
        }
        return subCriteria;
    }

    /**
     * direct copy of resolveJoinType since its private and we cant override  getOrCreateAlias since it calls it
     */
    org.hibernate.sql.JoinType resolveJoinType(JoinType joinType) {
        if(joinType  == null) {
            return org.hibernate.sql.JoinType.INNER_JOIN;
        }
        switch (joinType) {
            case JoinType.LEFT:
                return org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;
            case JoinType.RIGHT:
                return org.hibernate.sql.JoinType.RIGHT_OUTER_JOIN;
            default:
                return org.hibernate.sql.JoinType.INNER_JOIN;
        }
    }

    //these are compiledynamic hacks to work around permissions
    @CompileDynamic //so we can access protected
    String getCriteriaAndAliasAssociationPath(CriteriaAndAlias criteriaAndAlias){
        return criteriaAndAlias.associationPath
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
     * uses our custom hibernateAliasProjectionList
     */
    @Override
    List list() {
        def criteria = getHibernateCriteria()
        if(criteria == null) throw new IllegalStateException("Cannot execute query using a detached criteria instance");

        int projectionLength = initProjections()

        if (projectionLength < 2) {
            criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            applyDefaultSortOrderAndCaching()
            applyFetchStrategies()
        } else {
            //its a projection so make it a map instead of array
            criteria.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            //skip the listForCriteria
            //return criteria.list()
        }
        // FUTURE set a reasonable timeout to this
        // FIXME get from external config
        // criteria.setTimeout(15)
        //this fires Pre and PostQueryEvent, TODO might want to skip this if firing the events hurt performance
        return listForCriteria()

    }

    Boolean getHasJoins(){
        (Boolean)hasJoinsField.get(this)
    }

    @Override
    Object singleResult() {
        def criteria = getHibernateCriteria()
        if(criteria == null) throw new IllegalStateException("Cannot execute query using a detached criteria instance");

        int projectionLength = initProjections()
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        applyDefaultSortOrderAndCaching();
        applyFetchStrategies();

        Datastore datastore = session.getDatastore();
        ApplicationEventPublisher publisher = datastore.getApplicationEventPublisher();
        if(publisher != null) {
            publisher.publishEvent(new PreQueryEvent(datastore, this));
        }

        Object result;
        if(getHasJoins()) {
            try {
                result = proxyHandler.unwrap(criteria.uniqueResult());;
            } catch (NonUniqueResultException e) {
                result = singleResultViaListCall();
            }
        }
        else {
            result = singleResultViaListCall();
        }
        if(publisher != null) {
            publisher.publishEvent(new PostQueryEvent(datastore, this, Collections.singletonList(result)));
        }
        return result;
    }


    private Object singleResultViaListCall() {
        def criteria = getHibernateCriteria()
        criteria.setMaxResults(1);
        if(hibernateAliasProjectionList && hibernateAliasProjectionList.isRowCount()) {
            criteria.setFirstResult(0);
        }
        List results = criteria.list();
        if(results.size()>0) {
            return proxyHandler.unwrap(results.get(0));
        }
        return null;
    }

    /**
     * List but uses the CriteriaSpecification.ALIAS_TO_ENTITY_MAP for projection queries
     */
    List mapList() {
        def crit = getHibernateCriteria()
        if(crit == null) throw new IllegalStateException("Cannot execute query using a detached criteria instance");

        int projectionLength = initProjections()
        crit.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);

        // applyDefaultSortOrderAndCaching()
        // applyFetchStrategies()

        // return listForCriteria()
        return crit.list()
    }

    /**
     * get the hibernate ProjectionList for when customer query access is needed
     */
    org.hibernate.criterion.ProjectionList hibernateProjections(){
        def projs = projections() as HibernateAliasProjectionList
        return projs?.getHibernateProjectionList()
    }

    /**
     * Initialize and criteria.setProjection projectionList using the hibernateAliasProjectionList
     * @return the projectionList.length
     */
    int initProjections() {
        def crit = getHibernateCriteria()

        org.hibernate.criterion.ProjectionList projectionList = hibernateProjections()
        if (projectionList?.length) {
            crit.setProjection(projectionList)
            return projectionList.length
        }
        return 0
    }

    /**
     * override to use our hibernateAliasProjectionList by looking in
     */
    @Override
    protected void applyDefaultSortOrderAndCaching() {
        if(this.orderBy.isEmpty() && entity != null) {
            // don't apply default sorting, if projections present
            if(projections() != null && !projections().isEmpty()) return;

            Mapping mapping = AbstractGrailsDomainBinder.getMapping(entity.getJavaClass());
            if(mapping != null) {
                if(queryCache == null && mapping.getCache() != null && mapping.getCache().isEnabled()) {
                    getHibernateCriteria().setCacheable(true)
                }

                Map sortMap = mapping.getSort().getNamesAndDirections();
                DynamicFinder.applySortForMap(this, sortMap, true);

            }
        }
    }

    /**
     * returns the HibernateAliasProjectionList that is used to build the hibernateProjections
     */
    @Override
    Query.ProjectionList projections() {
        if (hibernateAliasProjectionList == null) {
            hibernateAliasProjectionList = new HibernateAliasProjectionList()
        }
        return hibernateAliasProjectionList;
    }

    @Override
    Object clone() {
        final CriteriaImpl impl = (CriteriaImpl) getHibernateCriteria();
        final HibernateSession hibernateSession = (HibernateSession) getSession();
        final GrailsHibernateTemplate hibernateTemplate = (GrailsHibernateTemplate) hibernateSession.getNativeInterface();
        return hibernateTemplate.execute{ Session session ->
            Criteria newCriteria = session.createCriteria(impl.getEntityOrClassName());

            Iterator iterator = impl.iterateExpressionEntries();
            while (iterator.hasNext()) {
                CriteriaImpl.CriterionEntry entry = (CriteriaImpl.CriterionEntry) iterator.next();
                newCriteria.add(entry.getCriterion());
            }
            Iterator subcriteriaIterator = impl.iterateSubcriteria();
            while (subcriteriaIterator.hasNext()) {
                CriteriaImpl.Subcriteria sub = (CriteriaImpl.Subcriteria) subcriteriaIterator.next();
                newCriteria.createAlias(sub.getPath(), sub.getAlias(), sub.getJoinType(), sub.getWithClause());
            }
            return new HibernateMangoQuery(newCriteria, hibernateSession, entity)
        }
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
            def hibProj = new HibernateProjectionAdapter(p).toHibernateProjection()
            if(p instanceof Query.PropertyProjection){
                String propName = (p as Query.PropertyProjection).propertyName
                // _projectionList.add(hibProj, propName.replace('.', '_'))
                String alias = buildAlias(p, propName)
                _projectionList.add(hibProj, alias)
            } else {
                //its CountProjection or IdProjection so add without alias
                _projectionList.add(hibProj)
            }

            return this
        }

        String buildAlias(Query.Projection p, String propName){
            propName = propName.replace('.', '_')
            switch (p) {
                case Query.SumProjection:
                    return "${propName}"
                case Query.CountProjection:
                    return "${propName}"
                case Query.AvgProjection:
                    return "${propName}"
                default:
                    return propName
            }
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
