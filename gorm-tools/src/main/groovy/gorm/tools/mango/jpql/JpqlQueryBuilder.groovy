/*
* Copyright 2011 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.jpql

import javax.persistence.criteria.JoinType

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.AbstractPersistentEntity
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.ToOne
import org.grails.datastore.mapping.query.AssociationQuery
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.AssociationCriteria
import org.grails.datastore.mapping.query.api.QueryableCriteria
import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.core.convert.ConversionService
import org.springframework.dao.InvalidDataAccessResourceUsageException

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.utils.GormMetaUtils
import yakworks.commons.map.Maps

/**
 * Builds JPQL String-based queries from the DetachedCriteria.
 * Used for projections and aggregate based queries as the criteria interface is limited there
 * Based on org.grails.datastore.mapping.query.jpa.JpaQueryBuilder
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@SuppressWarnings(['BuilderMethodWithSideEffects', 'AbcMetric', 'ParameterCount', 'ExplicitCallToEqualsMethod', 'ClassSize', 'MethodSize'])
@CompileStatic
class JpqlQueryBuilder {
    private static final String DISTINCT_CLAUSE = "DISTINCT "
    private static final String ORDER_BY_CLAUSE = " ORDER BY "
    private static final char COMMA = ','
    private static final char DOT = '.'
    private static final String PARAMETER_PREFIX = ":p"

    private Map<Class, QueryHandler> queryHandlers = [:]
    private Query.Junction criteria
    private Query.ProjectionList projectionList = new Query.ProjectionList()
    private List<Query.Order> orders = Collections.emptyList()
    //the main root entity alias, will normall be just the decapitalized name
    private String entityAlias

    // the root entity this is for
    private PersistentEntity entity

    //boolean hibernateCompatible

    //wraps the SELECT in a new map( ...) when true
    private boolean aliasToMap
    // deletes for example cant have joins
    private boolean allowJoins = true
    // if true then will use custom dialect functions such as flike
    private boolean enableDialectFunctions = false

    //map of join types from MangoDetachedCriteria
    Map<String, JoinType> joinTypes
    //aliases as they are built here
    Map<String, String> projectionAliases = [:]
    //map of propertyAliases from MangoDetachedCriteria
    Map<String, String> propertyAliases = [:]
    //as GroupPropertyProjection are proceses add them to this list so they can be skipped while building having clause
    private List<String> groupByList = []
    //as selects are proceses add them to this list so they can be skipped while building having clause
    private List<String> selectList = []

    ConversionService conversionService = ApplicationConversionService.getSharedInstance()

    JpqlQueryBuilder(PersistentEntity entity, Query.Junction criteria) {
        this.entity = entity
        this.criteria = criteria
        this.entityAlias = entity.getDecapitalizedName()
    }

    JpqlQueryBuilder(PersistentEntity entity) {
        this.entity = entity
        this.entityAlias = entity.getDecapitalizedName()
    }

    static JpqlQueryBuilder of(MangoDetachedCriteria crit){
        var jqb = new JpqlQueryBuilder(crit.persistentEntity, new Query.Conjunction(crit.criteria) )
        jqb.initHandlers()
        // List<Query.Criterion> criteria = crit.getCriteria()
        // jqb.criteria = new Query.Conjunction(criteria)
        jqb.propertyAliases = crit.propertyAliases

        jqb.joinTypes = Maps.clone(crit.getJoinTypes())

        List<Query.Projection> projections = crit.getProjections()
        for (Query.Projection projection : projections) {
            jqb.projectionList.add(projection)
        }

        jqb.orders = crit.getOrders()

        return jqb
    }

    JpqlQueryBuilder entityAlias(String v){
        this.entityAlias = v
        return this
    }

    /**
     * wraps the SELECT in a new map( ...)
     */
    JpqlQueryBuilder aliasToMap(boolean val){
        this.aliasToMap = val
        return this
    }

    /**
     * Enables custom dialect functions, such as flike
     */
    JpqlQueryBuilder enableDialectFunctions(boolean val){
        this.enableDialectFunctions = val
        return this
    }

    // public void setHibernateCompatible(boolean hibernateCompatible) {
    //     this.hibernateCompatible = hibernateCompatible
    // }

    /**
     * Builds an UPDATE statement.
     *
     * @param propertiesToUpdate THe properties to update
     * @return The JpaQueryInfo object
     */
    public JpqlQueryInfo buildUpdate(Map<String, Object> propertiesToUpdate) {
        if (propertiesToUpdate.isEmpty()) {
            throw new InvalidDataAccessResourceUsageException("No properties specified to update")
        }
        // allowJoins = false
        StringBuilder queryString = new StringBuilder("UPDATE ${entity.getName()} ${entityAlias}")

        List parameters = []
        buildUpdateStatement(queryString, propertiesToUpdate, parameters)
        StringBuilder whereClause = new StringBuilder()
        buildWhereClause(queryString, whereClause, entityAlias, parameters)
        return new JpqlQueryInfo(queryString.toString(), parameters)
    }

    /**
     * Builds a DELETE statement
     *
     * @return The JpaQueryInfo
     */
    public JpqlQueryInfo buildDelete() {
        StringBuilder queryString = new StringBuilder("DELETE ${entity.getName()} ${entityAlias}")
        StringBuilder whereClause = new StringBuilder()
        allowJoins = false
        List parameters = buildWhereClause(queryString, whereClause, entityAlias)
        return new JpqlQueryInfo(queryString.toString(), parameters)
    }

    /**
     * Builds  SELECT statement
     *
     * @return The JpaQueryInfo
     */
    JpqlQueryInfo buildSelect() {
        StringBuilder queryString = new StringBuilder("SELECT ")

        buildSelectClause(queryString)

        StringBuilder whereClause= new StringBuilder()
        List parameters = []
        if (!criteria.isEmpty()) {
            parameters = buildWhereClause(queryString, whereClause, entityAlias, parameters)
        }

        buildGroup(queryString)

        if (!criteria.isEmpty() && projectionAliases) {
            buildHavingClause(queryString, entityAlias, parameters)
        }

        appendOrder(queryString, entityAlias)

        var jqInfo = new JpqlQueryInfo(queryString.toString(), parameters)
        jqInfo.where = whereClause.toString()

        return jqInfo
    }

    void buildGroup(StringBuilder queryString){
        if(groupByList){
            queryString.append(" GROUP BY ")
            for (Iterator i = groupByList.iterator(); i.hasNext();) {
                String val = (String) i.next()
                queryString.append("$val")
                if (i.hasNext()) {
                    queryString.append(COMMA)
                }
            }
        }
    }

    private void buildSelectClause(StringBuilder queryString) {
        Query.ProjectionList projectionList = this.projectionList
        String logicalName = this.entityAlias
        PersistentEntity entity = this.entity
        buildSelect(queryString, projectionList.getProjectionList(), logicalName, entity)

        queryString.append(" FROM ${entity.getName()} AS ${logicalName}")
        joinTypes.each { k, v ->
            if(v == JoinType.LEFT){
                queryString.append(" LEFT JOIN ${logicalName}.${k}")
            }
        }
    }

    void appendAlias( StringBuilder queryString, String projField, String name, String aliasPrefix){
        String aliasKey = "${aliasPrefix}_${name}"
        String propalias = propertyAliases.containsKey(aliasKey) ? propertyAliases[aliasKey] : name.replace('.', '_')
        propalias = "${propalias}"
        queryString
            .append(projField)
            .append(' as ')
            .append(propalias)
        projectionAliases[propalias] = projField
    }

    void buildSelect(StringBuilder queryString, List<Query.Projection> projectionList, String logicalName, PersistentEntity entity) {
        if (projectionList.isEmpty()) {
            queryString.append(DISTINCT_CLAUSE)
                    .append(logicalName)
        }
        else {
            if(aliasToMap){
                queryString.append("new map( ")
            }
            for (Iterator i = projectionList.iterator(); i.hasNext();) {
                Query.Projection projection = (Query.Projection) i.next()
                if (projection instanceof Query.CountProjection) {
                    String projField = "COUNT(${logicalName})"
                    appendAlias(queryString, projField, logicalName, 'COUNT')
                }
                else if (projection instanceof Query.IdProjection) {
                    queryString.append(logicalName)
                            .append(DOT)
                            .append(entity.getIdentity().getName())
                    //queryString.append("1")
                }
                else if (projection instanceof Query.PropertyProjection) {
                    Query.PropertyProjection pp = (Query.PropertyProjection) projection
                    if (projection instanceof Query.AvgProjection) {
                        String projField = "AVG(${logicalName}.${pp.getPropertyName()})"
                        appendAlias(queryString, projField, pp.getPropertyName(), 'AVG')
                    }
                    else if (projection instanceof Query.SumProjection) {
                        String projField = "SUM(${logicalName}.${pp.getPropertyName()})"
                        appendAlias(queryString, projField, pp.getPropertyName(), 'SUM')
                    }
                    else if (projection instanceof Query.MinProjection) {
                        String projField = "MIN(${logicalName}.${pp.getPropertyName()})"
                        appendAlias(queryString, projField, pp.getPropertyName(), 'MIN')
                    }
                    else if (projection instanceof Query.MaxProjection) {
                        String projField = "MAX(${logicalName}.${pp.getPropertyName()})"
                        appendAlias(queryString, projField, pp.getPropertyName(), 'MAX')
                    }
                    else if (projection instanceof Query.CountDistinctProjection) {
                        String projField = "COUNT(DISTINCT ${logicalName}.${pp.getPropertyName()})"
                        appendAlias(queryString, projField, pp.getPropertyName(), 'COUNT')
                    }
                    else if (projection instanceof Query.DistinctPropertyProjection) {
                        String projField = "DISTINCT ${logicalName}.${pp.getPropertyName()}"
                        appendAlias(queryString, projField, pp.getPropertyName(), '')
                    }
                    else if (projection instanceof Query.GroupPropertyProjection) {
                        String projField = "${logicalName}.${pp.getPropertyName()}"
                        appendAlias(queryString, projField, pp.getPropertyName(), '')
                        groupByList << "${logicalName}.${pp.getPropertyName()}".toString()
                    }
                    else { //assume its just a property to add to the select
                        String projField = "${logicalName}.${pp.getPropertyName()}"
                        appendAlias(queryString, projField, pp.getPropertyName(), '')
                        selectList << "${logicalName}.${pp.getPropertyName()}".toString()
                    }
                }

                if (i.hasNext()) {
                    queryString.append(COMMA)
                    queryString.append(' ')
                }
            }

            if(aliasToMap){
                queryString.append(" )")
            }
        }
    }

    int appendCriteriaForOperator(StringBuilder q, String logicalName, final String name, int position, String operator) {
        //if its a projectionAlias then use it
        String propName = buildPropName(name, logicalName)

        q.append(propName)
         .append(operator)
         .append(PARAMETER_PREFIX)
         .append(++position)
        return position
    }

    /**
     * Build a property name like kitchenSink.amount for 'amount'
     * Will use the projection alias if exists
     */
    String buildPropName(String name, String logicalName) {
        String propName
        if(projectionAliases.containsKey(name)) {
            propName = projectionAliases[name]
        } else if(logicalName && !name.startsWith(logicalName + DOT)) {
            propName = logicalName + DOT + name
        }
        else {
            propName = name
        }
        return propName
    }

    QueryHandler getCompareQueryHandler(String compareOp){
        return new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                              String logicalName, int position, List parameters) {
                Query.PropertyCriterion opCriterion = (Query.PropertyCriterion) criterion
                String name = opCriterion.getProperty()
                PersistentProperty prop = validateProperty(entity, name, opCriterion.class.simpleName)
                int newPosition = appendCriteriaForOperator(whereClause, logicalName, name, position, compareOp)
                if(prop){
                    Class propType = prop.getType()
                    parameters.add(conversionService.convert( opCriterion.getValue(), propType ))
                } else {
                    parameters.add(opCriterion.getValue())
                }
                return newPosition
            }
        }
    }

    //Property compares
    QueryHandler getPropertyCompareQueryHandler(String compareOp){
        new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                              String logicalName, int position, List parameters) {
                Query.PropertyComparisonCriterion opCriterion = (Query.PropertyComparisonCriterion) criterion
                //handlePropCompare(entity, opCriterion, logicalName, compareOp,  whereClause)
                String propertyName = opCriterion.getProperty()
                String otherProperty = opCriterion.getOtherProperty()

                validateProperty(entity, propertyName, criterion.class.simpleName)
                validateProperty(entity, otherProperty, criterion.class.simpleName)
                appendPropertyComparison(whereClause, logicalName, propertyName, otherProperty, compareOp)

                return position
            }
        }
    }

    //Is Null, Is Empty, IS Not Null, etc...
    QueryHandler getISCheckQueryHandler(String compareOp){
        new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                              String logicalName, int position, List parameters) {
                Query.PropertyNameCriterion ischeck = (Query.PropertyNameCriterion) criterion
                final String name = ischeck.getProperty()
                validateProperty(entity, name, compareOp)

                String propName = buildPropName(name, logicalName)
                whereClause.append(propName).append(compareOp)

                return position
            }
        }
    }

    //Is Null, Is Empty, IS Not Null, etc...
    QueryHandler getSubQueryHandler(String compareOp){
        new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                              String logicalName, int position, List parameters) {
                Query.SubqueryCriterion subqueryCriterion = (Query.SubqueryCriterion) criterion
                return handleSubQuery(entity, q, whereClause, logicalName, position, parameters, subqueryCriterion, compareOp)
            }
        }
    }

    void initHandlers(){

        //Normal compares
        queryHandlers.put(Query.Equals, getCompareQueryHandler("="))
        queryHandlers.put(Query.NotEquals, getCompareQueryHandler(" != "))
        queryHandlers.put(Query.GreaterThan, getCompareQueryHandler(" > "))
        queryHandlers.put(Query.LessThan, getCompareQueryHandler(" < "))
        queryHandlers.put(Query.LessThanEquals, getCompareQueryHandler(" <= "))
        queryHandlers.put(Query.GreaterThanEquals, getCompareQueryHandler(" >= "))
        queryHandlers.put(Query.Like, getCompareQueryHandler(" like "))

        //Property compares
        queryHandlers.put(Query.EqualsProperty, getPropertyCompareQueryHandler(" = "))
        queryHandlers.put(Query.NotEqualsProperty, getPropertyCompareQueryHandler(" != "))
        queryHandlers.put(Query.GreaterThanProperty, getPropertyCompareQueryHandler(" > "))
        queryHandlers.put(Query.LessThanProperty, getPropertyCompareQueryHandler(" < "))
        queryHandlers.put(Query.LessThanEqualsProperty, getPropertyCompareQueryHandler(" <= "))
        queryHandlers.put(Query.GreaterThanEqualsProperty, getPropertyCompareQueryHandler(" >= "))

        //Is Null, Is Empty, IS Not Null, etc...
        queryHandlers.put(Query.IsNull, getISCheckQueryHandler(" IS NULL "))
        queryHandlers.put(Query.IsNotNull, getISCheckQueryHandler(" IS NOT NULL "))
        queryHandlers.put(Query.IsEmpty, getISCheckQueryHandler(" IS EMPTY "))
        queryHandlers.put(Query.IsNotEmpty, getISCheckQueryHandler(" IS NOT EMPTY "))

        //sub queries, not all of these supported by Mango but here for future use
        queryHandlers.put(Query.NotIn, getSubQueryHandler(" NOT IN ("))
        queryHandlers.put(Query.EqualsAll, getSubQueryHandler(" = ALL ("))
        queryHandlers.put(Query.NotEqualsAll, getSubQueryHandler(" != ALL ("))
        queryHandlers.put(Query.GreaterThanAll, getSubQueryHandler(" > ALL ("))
        queryHandlers.put(Query.GreaterThanSome, getSubQueryHandler(" > SOME ("))
        queryHandlers.put(Query.GreaterThanEqualsAll, getSubQueryHandler(" >= ALL ("))
        queryHandlers.put(Query.GreaterThanEqualsSome, getSubQueryHandler(" >= SOME ("))
        queryHandlers.put(Query.LessThanAll, getSubQueryHandler(" < ALL ("))
        queryHandlers.put(Query.LessThanSome, getSubQueryHandler(" < SOME ("))
        queryHandlers.put(Query.LessThanEqualsAll, getSubQueryHandler(" <= ALL ("))
        queryHandlers.put(Query.LessThanEqualsSome, getSubQueryHandler(" <= SOME ("))


        queryHandlers.put(AssociationQuery, new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                              String logicalName, int position, List parameters) {

                if (!allowJoins) {
                    throw new InvalidDataAccessResourceUsageException("Joins cannot be used in a DELETE or UPDATE operation")
                }
                AssociationQuery aq = (AssociationQuery) criterion
                final Association<?> association = aq.getAssociation()
                Query.Junction associationCriteria = aq.getCriteria()
                List<Query.Criterion> associationCriteriaList = associationCriteria.getCriteria()

                return handleAssociationCriteria(q, whereClause, logicalName, position, parameters,
                    association, associationCriteria, associationCriteriaList)
            }
        })

        queryHandlers.put(Query.Negation, new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                              String logicalName, int position, List parameters) {

                whereClause.append("NOT (")

                final Query.Negation negation = (Query.Negation)criterion
                position = buildWhereClauseForCriterion(entity, negation, q, whereClause, logicalName, negation.getCriteria(), position,
                    parameters)
                whereClause.append(")")

                return position
            }
        })

        queryHandlers.put(Query.Conjunction, new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion,
                              StringBuilder q, StringBuilder whereClause, String logicalName, int position, List parameters) {
                whereClause.append("(")

                final Query.Conjunction conjunction = (Query.Conjunction)criterion
                position = buildWhereClauseForCriterion(entity, conjunction, q, whereClause, logicalName, conjunction.getCriteria(),
                    position, parameters)
                whereClause.append(")")

                return position
            }
        })

        queryHandlers.put(Query.Disjunction, new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion,
                              StringBuilder q, StringBuilder whereClause, String logicalName, int position, List parameters) {
                whereClause.append("(")

                final Query.Disjunction disjunction = (Query.Disjunction)criterion
                position = buildWhereClauseForCriterion(entity, disjunction, q, whereClause,  logicalName, disjunction.getCriteria(),
                    position, parameters)
                whereClause.append(")")

                return position
            }
        })

        queryHandlers.put(Query.IdEquals, new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                              String logicalName, int position, List parameters) {
                Query.IdEquals eq = (Query.IdEquals) criterion
                PersistentProperty prop = entity.getIdentity()
                Class propType = prop.getType()
                position = appendCriteriaForOperator(whereClause, logicalName, prop.getName(), position, "=")
                parameters.add(conversionService.convert( eq.getValue(), propType ))
                return position
            }
        })

        queryHandlers.put(Query.Between, new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                              String logicalName, int position, List parameters) {
                Query.Between between = (Query.Between) criterion
                final Object from = between.getFrom()
                final Object to = between.getTo()

                final String name = between.getProperty()
                PersistentProperty prop = validateProperty(entity, name, Query.Between.simpleName)
                Class propType = prop.getType()

                String propName = buildPropName(name, logicalName)

                whereClause.append("(")
                           .append(propName)
                           .append(" >= ")
                           .append(PARAMETER_PREFIX)
                           .append(++position)
                whereClause.append(" AND ")
                           .append(propName)
                           .append(" <= ")
                           .append(PARAMETER_PREFIX)
                           .append(++position)
                           .append(")")

                parameters.add(conversionService.convert( from, propType ))
                parameters.add(conversionService.convert( to, propType ))
                return position
            }
        })

        // ILIKE SQL SERVER
        queryHandlers.put(Query.ILike, new QueryHandler() {

            public int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                              String logicalName, int position, List parameters) {
                Query.ILike eq = (Query.ILike) criterion
                final String name = eq.getProperty()
                PersistentProperty prop = validateProperty(entity, name, Query.ILike.simpleName)
                Class propType = prop.getType()

                String propName = buildPropName(name, logicalName)

                if(enableDialectFunctions){
                    whereClause
                        .append('flike(')
                        .append(propName)
                        .append(", ")
                        .append(PARAMETER_PREFIX)
                        .append(++position)
                        //.append(" ) = true")
                        .append(" ) = true")
                } else {
                    whereClause.append("lower(")
                    whereClause
                        .append(propName)
                        .append(")")
                        .append(" like lower(")
                        .append(PARAMETER_PREFIX)
                        .append(++position)
                        .append(")")
                }

                parameters.add(conversionService.convert( eq.getValue(), propType ))
                return position
            }
        })

        queryHandlers.put(Query.In, new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                              String logicalName, int position, List parameters) {
                Query.In inQuery = (Query.In) criterion
                final String name = inQuery.getProperty()
                PersistentProperty prop = validateProperty(entity, name, Query.In.simpleName)
                Class propType = prop.getType()

                String propName = buildPropName(name, logicalName)

                whereClause.append(propName).append(" IN (")
                QueryableCriteria subquery = inQuery.getSubquery()
                if(subquery != null) {
                    buildSubQuery(q, whereClause, position, parameters, subquery)
                }
                else {
                    for (Iterator i = inQuery.getValues().iterator(); i.hasNext();) {
                        Object val = i.next()
                        whereClause.append(PARAMETER_PREFIX)
                        whereClause.append(++position)
                        if (i.hasNext()) {
                            whereClause.append(COMMA)
                        }
                        parameters.add(conversionService.convert(val, propType))
                    }
                }
                whereClause.append(")")

                return position
            }
        })

        queryHandlers.put(Query.Exists, new QueryHandler() {
            public int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                              String logicalName, int position, List parameters) {
                Query.Exists existsQuery = (Query.Exists) criterion

                whereClause.append("EXISTS ( ")
                QueryableCriteria subquery = existsQuery.getSubquery()
                if (subquery != null) {
                    position = buildSubQuery(q, whereClause, position, parameters, subquery)
                }
                whereClause.append(" ) ")
                return position
            }
        })

    }

    int handleSubQuery(PersistentEntity entity, StringBuilder q, StringBuilder whereClause, String logicalName, int position, List parameters,
                       Query.SubqueryCriterion equalsAll, String comparisonExpression) {
        final String name = equalsAll.getProperty()
        validateProperty(entity, name, Query.In.simpleName)
        QueryableCriteria subquery = equalsAll.getValue()
        whereClause.append(logicalName)
                .append(DOT)
                .append(name)
                .append(comparisonExpression)
        buildSubQuery(q, whereClause, position, parameters, subquery)
        whereClause.append(")")
        return position
    }

    int buildSubQuery(StringBuilder q, StringBuilder whereClause, int position, List parameters,
                       QueryableCriteria subquery) {
        PersistentEntity associatedEntity = subquery.getPersistentEntity()
        String associatedEntityName = associatedEntity.getName()
        String associatedEntityLogicalName = associatedEntity.getDecapitalizedName() + position
        whereClause.append("SELECT ")
        buildSelect(whereClause, subquery.getProjections(), associatedEntityLogicalName, associatedEntity)
        whereClause.append(" FROM ${associatedEntityName} ${associatedEntityLogicalName} WHERE ")

        List<Query.Criterion> criteria = subquery.getCriteria()
        var conj = new Query.Conjunction(criteria)
        position = buildWhereClauseForCriterion(associatedEntity, conj, q, whereClause, associatedEntityLogicalName,
            criteria, position, parameters)
        return position
        // JpqlQueryBuilder subQueryBuilder = new  JpqlQueryBuilder(associatedEntity)
    }

    int handleAssociationCriteria(StringBuilder query, StringBuilder whereClause, String logicalName, int position, List parameters,
                                  Association<?> association, Query.Junction associationCriteria, List<Query.Criterion> associationCriteriaList) {
        if (association instanceof ToOne) {
            final String associationName = association.getName()
            logicalName = "${logicalName}.${associationName}"
            // JpqlQueryBuilder assocQueryBuilder = new  JpqlQueryBuilder(association.getAssociatedEntity())
            return buildWhereClauseForCriterion(association.getAssociatedEntity(), associationCriteria, query, whereClause, logicalName,
                associationCriteriaList, position, parameters)
        }
        else if (association != null) {
            final String associationName = association.getName()
            // TODO: Allow customization of join strategy!
            query.append(" INNER JOIN ${logicalName}.${associationName} ${associationName}")

            return buildWhereClauseForCriterion(association.getAssociatedEntity(), associationCriteria, query, whereClause, associationName,
                associationCriteriaList, position, parameters)
        }

        return position
    }

    private void buildUpdateStatement(StringBuilder queryString, Map<String, Object> propertiesToUpdate, List parameters) {
        queryString.append(" SET")

        // keys need to be sorted before query is built
        Set<String> keys = new TreeSet<String>(propertiesToUpdate.keySet())

        Iterator<String> iterator = keys.iterator()
        while (iterator.hasNext()) {
            String propertyName = iterator.next()
            PersistentProperty prop = entity.getPropertyByName(propertyName)
            if (prop == null) throw new InvalidDataAccessResourceUsageException("""\
                Property '${propertyName}' of class '${entity.getName()}' specified in update does not exist
            """.stripIndent())

            parameters.add(propertiesToUpdate.get(propertyName))
            queryString.append(" ${entityAlias}.${propertyName}=")
            queryString.append(PARAMETER_PREFIX).append(parameters.size())
            if (iterator.hasNext()) {
                queryString.append(COMMA)
            }
        }
    }

    void appendPropertyComparison(StringBuilder q, String logicalName, String propertyName, String otherProperty,
                                                 String operator) {
        q.append(logicalName)
         .append(DOT)
         .append(propertyName)
         .append(operator)

        //FIXME hack for now, MangoDetachedCriteria defualt to the alias = enityName with _ suffix
        // if the other property ends with _ and removnig that matches the logicalName then its the alias
        // used in EXISTS query where there is a subquery and we are tying it together.
        int dotIdx = otherProperty.indexOf(".")
        String rootObj = dotIdx > -1 ? otherProperty.substring(0, dotIdx) : otherProperty
        if(rootObj.endsWith('_') && rootObj[0..-2] == this.entityAlias){
            String restOfPath = dotIdx > -1 ? otherProperty.substring(dotIdx) : ""
            //[0..-2] removes last char from string
            q.append(rootObj[0..-2])
                .append(restOfPath)
        }
        else if (rootObj == this.entityAlias){
            String restOfPath = dotIdx > -1 ? otherProperty.substring(dotIdx) : ""
            q.append(rootObj)
                .append(restOfPath)
        }
        else {
            q.append(logicalName).append(DOT).append(otherProperty)
        }

    }

    PersistentProperty validateProperty(PersistentEntity entity, String name, String whatItChecks) {
        if(name.endsWith('.id') && name.count('.') >= 1){
            return GormMetaUtils.getPersistentProperty(entity, name)
        }
        // if(name.endsWith('.id') && name.count('.') == 1) {
        //     String assoc = name.tokenize('.')[0]
        //     return (entity.getPropertyByName(assoc) as Association).getAssociatedEntity().getIdentity()
        // }

        PersistentProperty identity = entity.getIdentity()
        if (identity != null && identity.getName().equals(name)) {
            return identity
        }
        PersistentProperty[] compositeIdentity = ((AbstractPersistentEntity) entity).getCompositeIdentity()
        if(compositeIdentity != null) {
            for (PersistentProperty property : compositeIdentity) {
                if(property.getName().equals(name)) {
                    return property
                }
            }
        }
        PersistentProperty prop = entity.getPropertyByName(name)
        boolean isAliasProp = projectionAliases.containsKey(name)
        if (prop == null && !isAliasProp) {
            throw new InvalidDataAccessResourceUsageException("Cannot use [${whatItChecks}] criterion on non-existent property: " + name)
        }
        return prop
    }

    private List buildWhereClause(StringBuilder q, StringBuilder whereClause, String logicalName, List parameters = []) {
        if (!criteria.isEmpty()) {
            int position = parameters.size()
            final List<Query.Criterion> criterionList = criteria.getCriteria()
            StringBuilder tempWhereClause = new StringBuilder()

            position = buildWhereClauseForCriterion(entity, criteria, q, tempWhereClause, logicalName, criterionList, position, parameters)

            //if it didn't build anything then dont add it
            if(tempWhereClause.toString()) {
                q.append(" WHERE ")
                if (criteria instanceof Query.Negation) {
                    whereClause.append("NOT (")
                }
                //whereClause.append("(")
                whereClause.append(tempWhereClause.toString())
                //whereClause.append(")")
                //close parens
                if (criteria instanceof Query.Negation) {
                    whereClause.append(")")
                }

                q.append(whereClause.toString())
            }
        }
        return parameters
    }

    private List buildHavingClause(StringBuilder q, String logicalName, List parameters) {
        if (!criteria.isEmpty() && projectionAliases) {
            int position = parameters.size()
            final List<Query.Criterion> criterionList = criteria.getCriteria()

            StringBuilder tempHavingClause = new StringBuilder()
            position = buildHavingClauseForCriterion(q, tempHavingClause, logicalName, criterionList, position, parameters)

            if(tempHavingClause.toString()) {
                q.append(" HAVING ")
                q.append("(")
                q.append(tempHavingClause.toString())
                q.append(")")
            }
        }
        return parameters
    }

    protected void appendOrder(StringBuilder queryString, String logicalName) {
        if (!orders.isEmpty()) {
            queryString.append( ORDER_BY_CLAUSE)
            for (Query.Order order : orders) {
                //if its not in aliases then add the entityName
                if(!projectionAliases.containsKey(order.getProperty())){
                    queryString.append(logicalName)
                        .append(DOT)
                }
                queryString.append("${order.getProperty()} ${order.getDirection().toString()} ")
            }
        }
    }

    // int buildWhereClauseForCriterion(JpqlQueryBuilder subQueryBuilder, Query.Junction criteria, StringBuilder q, StringBuilder whereClause,
    //                                  String logicalName, final List<Query.Criterion> criterionList, int position, List parameters) {
    //
    //     return buildWhereClauseForCriterion(subQueryBuilder.entity, criteria, q, whereClause, logicalName, criterionList, position, parameters)
    // }

    int buildWhereClauseForCriterion(PersistentEntity persistentEntity, Query.Junction criteria, StringBuilder q, StringBuilder whereClause,
                                     String logicalName, final List<Query.Criterion> criterionList, int position, List parameters) {

        Map clauseTokens = [:] as Map<String,String>

        for (Iterator<Query.Criterion> iterator = criterionList.iterator(); iterator.hasNext();) {
            StringBuilder tempWhereClause = new StringBuilder()
            Query.Criterion criterion = iterator.next()

            //TODO handle it for situations like below
            //select sum(amount) as amount from artran where amount> 100 group by trantypeid ; VS
            //select sum(amount) as amount from artran group by trantypeid having sum(amount) > 100;

            //makes sure not to pick up criteria that should be going into the HAVING clause
            if(criterion instanceof Query.PropertyNameCriterion){
                String prop = criterion.getProperty()
                String groupProp = logicalName ? "${logicalName}.${prop}" : prop
                //groupBy should never be in HAVING and if they are just property selects dont skip either
                if(projectionAliases.containsKey(prop) && !groupByList.contains(groupProp) && !selectList.contains(groupProp)){
                    continue
                }
            }
            final String operator = criteria instanceof Query.Conjunction ? " AND " : " OR "

            QueryHandler qh = queryHandlers.get(criterion.getClass())
            if (qh != null) {

                position = qh.handle(persistentEntity, criterion, q, tempWhereClause, logicalName,
                        position, parameters)
            }
            else if (criterion instanceof AssociationCriteria) {

                if (!allowJoins) {
                    throw new InvalidDataAccessResourceUsageException("Joins cannot be used in a DELETE or UPDATE operation")
                }
                AssociationCriteria ac = (AssociationCriteria) criterion
                Association association = ac.getAssociation()
                List<Query.Criterion> associationCriteriaList = ac.getCriteria()
                position = handleAssociationCriteria(q, tempWhereClause, logicalName, position, parameters, association,
                    new Query.Conjunction(), associationCriteriaList)
            }
            else {
                throw new InvalidDataAccessResourceUsageException("Queries of type "+
                    criterion.getClass().getSimpleName()+" are not supported by this implementation")
            }

            String toStringWhere = tempWhereClause
            if(toStringWhere) clauseTokens[toStringWhere] = operator
            // if (whereClause.toString() && iterator.hasNext()) {
            //     whereClause.append(operator)
            // }
        }

        for (Iterator<String> iterator = clauseTokens.keySet().iterator(); iterator.hasNext();) {
            String key = iterator.next()
            String operator = clauseTokens[key]
            whereClause.append(key)
            if(iterator.hasNext()) whereClause.append(operator)
        }

        return position
    }

    int buildHavingClauseForCriterion(StringBuilder q, StringBuilder whereClause,
                                      String logicalName, final List<Query.Criterion> criterionList, int position, List parameters) {

        Map clauseTokens = [:] as Map<String,String>

        for (Iterator<Query.Criterion> iterator = criterionList.iterator(); iterator.hasNext();) {
            StringBuilder tempWhereClause = new StringBuilder()
            Query.Criterion criterion = iterator.next()
            //skip if its anything but a projection alias
            boolean isPropCrit = criterion instanceof Query.PropertyNameCriterion
            if(isPropCrit){
                //skip if its a groupby or it has an alias
                String prop = (criterion as Query.PropertyNameCriterion).getProperty()
                String groupProp = logicalName ? "${logicalName}.${prop}" : prop
                boolean isGrouped = groupByList.contains(groupProp)
                boolean isSelect = selectList.contains(groupProp)
                boolean hasAlias = projectionAliases.containsKey(prop)

                if(isGrouped || isSelect || !hasAlias){
                    continue
                }
            } else {
                continue
            }
            final String operator = criteria instanceof Query.Conjunction ? " AND " : " OR "

            QueryHandler qh = queryHandlers.get(criterion.getClass())
            if (qh != null) {
                position = qh.handle(this.entity, criterion, q, tempWhereClause, '', position, parameters)
            }
            else if (criterion instanceof AssociationCriteria) {

                if (!allowJoins) {
                    throw new InvalidDataAccessResourceUsageException("Joins cannot be used in a DELETE or UPDATE operation")
                }
                AssociationCriteria ac = (AssociationCriteria) criterion
                Association association = ac.getAssociation()
                List<Query.Criterion> associationCriteriaList = ac.getCriteria()
                position = handleAssociationCriteria(q, tempWhereClause, logicalName, position, parameters, association,
                    new Query.Conjunction(), associationCriteriaList)
            }
            else {
                throw new InvalidDataAccessResourceUsageException("Queries of type "+
                    criterion.getClass().getSimpleName()+" are not supported by this implementation")
            }

            String toStringWhere = tempWhereClause
            if(toStringWhere) clauseTokens[toStringWhere] = operator
        }

        for (Iterator<String> iterator = clauseTokens.keySet().iterator(); iterator.hasNext();) {
            String key = iterator.next()
            String operator = clauseTokens[key]
            whereClause.append(key)
            if(iterator.hasNext()) whereClause.append(operator)
        }

        return position
    }
}
