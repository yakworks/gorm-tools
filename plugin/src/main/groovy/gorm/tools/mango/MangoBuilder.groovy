/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.codehaus.groovy.runtime.InvokerHelper
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.QueryableCriteria

import gorm.tools.beans.IsoDateUtil
import gorm.tools.mango.api.QueryMangoEntity
import grails.gorm.DetachedCriteria

/**
 * the main builder to turn Mango QL maps and json into DetachedCriteria for Gorm
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
@Slf4j
class MangoBuilder {
    //DetachedCriteria criteria

    static final Map<String, String> CompareOps = [
        '$gt'    : 'gt',
        '$eq'    : 'eq',
        '$gte'   : 'ge',
        '$lt'    : 'lt',
        '$lte'   : 'le',
        '$ne'    : 'ne',
        '$not'   : 'not',
        '$ilike' : 'ilike',
        '$like'  : 'like',
        '$in'    : 'in',
        '$inList': 'inList'
    ]

    static final Map<String, String> PropertyOps = [
        '$gtf' : 'gtProperty',
        '$gtef': 'geProperty',
        '$ltf' : 'ltProperty',
        '$ltef': 'leProperty',
        '$eqf' : 'eqProperty',
        '$nef' : 'neProperty'
    ]

    static final Map<String, String> OverrideOps = [
        '$between': 'between',
        '$nin'    : 'notIn'
    ]

    static final Map<String, String> JunctionOps = [
        '$and': 'and',
        '$or' : 'or',
        '$not': 'not'
    ]

    static final Map<String, String> ExistOps = [
        '$isNull'   : 'isNull',
        '$isNotNull': 'isNotNull'
    ]

    static final Map<String, String> QuickSearchOps = [
        '$quickSearch': 'quickSearch',
        '$q'          : 'quickSearch'
    ]

    static final Map<String, String> SortOps = [
        '$sort': 'order'
    ]

    static <D> DetachedCriteria<D> build(Class<D> clazz, Map map, Closure callable = null) {
        DetachedCriteria<D> detachedCriteria = new DetachedCriteria<D>(clazz)
        return build(detachedCriteria, map, callable)
    }

    static <D> DetachedCriteria<D> build(DetachedCriteria<D> criteria, Map map, Closure callable = null) {
        DetachedCriteria newCriteria = cloneCriteria(criteria)
        applyMapOrList(newCriteria, MangoTidyMap.tidy(map))
        if (callable) newCriteria.with callable
        return newCriteria
    }

    @CompileDynamic //dynamic so it can access the protected criteria.clone
    static <D> DetachedCriteria<D> cloneCriteria(DetachedCriteria<D> criteria) {
        criteria.clone()
    }

    static void applyMapOrList(DetachedCriteria criteria, Object mapOrList) {
        if (mapOrList instanceof Map) {
            applyMap(criteria, mapOrList)
        } else if (mapOrList instanceof List<Map>) {
            for (Map item : mapOrList as List<Map>) {
                applyMap(criteria, item)
            }
        } else {
            throw new IllegalArgumentException("Must be a map or a list")
        }
    }

    /**
     * applies the map just like running a closure.call on this.
     * @param mangoMap
     */
    static void applyMap(DetachedCriteria criteria, Map mangoMap) {
        log.debug "applyMap $mangoMap"
        for (String key : mangoMap.keySet()) {
            String op = JunctionOps[key]
            if (op) {
                //normalizer should have ensured all ops have a List for a value
                //List args = [criteria, (List) mangoMap[key]]
                invoke(op, criteria, (List) mangoMap[key])
                //MangoBuilder.metaClass.invokeStaticMethod(MangoBuilder, op, args)
                //"$op"(criteria, (List) mangoMap[key])
                continue
            } else { //it must be a field then
                applyField(criteria, key, mangoMap[key])
            }
        }
    }

    static Object invoke(String op, Object... args) {
        InvokerHelper.invokeStaticMethod(MangoBuilder, op, args)
    }

    static void applyField(DetachedCriteria criteria, String field, Object fieldVal) {
        String qs = QuickSearchOps[field]
        if (qs) {
            invoke(qs, criteria, fieldVal)
            //this."$qs"(criteria, fieldVal)
            return
        }

        String sort = SortOps[field]
        if (sort) {
            order(criteria, fieldVal)
        }

        PersistentProperty prop = criteria.persistentEntity.getPropertyByName(field)

        if(prop == null) {
            log.info("invalid domain property $field")
            return
        }
        //if its an association then call it as a method so methodmissing will pick it up and build the DetachedAssocationCriteria
        if (prop instanceof Association) {
            //invoke(field, criteria, fieldVal)
            criteria.invokeMethod(field){
                //the delegate is the DetachedAssocationCriteria. See methodMissing in AbstractDetachedCriteria
                applyMapOrList((DetachedCriteria) delegate, fieldVal)
                return
            }

        }
        // if field ends in Id then try removing the Id postfix and see if its a property
        else if (field.matches(/.*[^.]Id/) && criteria.persistentEntity.getPropertyByName(field.replaceAll("Id\$", ""))) {
            applyField(criteria, field.replaceAll("Id\$", ""), ['id': fieldVal])
        } else if (!(fieldVal instanceof Map) && !(fieldVal instanceof List)) {
            criteria.eq(field, toType(criteria, field, fieldVal))
        } else if (fieldVal instanceof Map) { // could be field=name fieldVal=['$like': 'foo%']
            //could be 1 or more too
            //for example field=amount and fieldVal=['$lt': 100, '$gt':200]
            for (String key : (fieldVal as Map).keySet()) {
                //everything has to either be either a junction op or condition
                Object opArg = fieldVal[key]

                String op = JunctionOps[key]
                if (op) {
                    //normalizer should have ensured all ops have a List for a value
                    invoke(op, criteria, (List) opArg)
                    //this."$op"(criteria, (List) opArg)
                    continue
                }

                op = OverrideOps[key]
                if (op) {
                    invoke(op, criteria, field, toType(criteria, field, opArg))
                    //this."$op"(criteria, field, toType(criteria, field, opArg))
                    continue
                }

                op = CompareOps[key]
                if (op) {
                    if (opArg == null) {
                        criteria.isNull(field)
                        continue
                    }
                    criteria.invokeMethod(op, [field, toType(criteria, field, opArg)])
                    //criteria."$op"(field, toType(criteria, field, opArg))
                    continue
                }

                op = PropertyOps[key]
                if (op) {
                    criteria.invokeMethod(op, [field, opArg])
                    //criteria."$op"(field, opArg)
                    continue
                }

                op = ExistOps[key]
                if (op) {
                    criteria.invokeMethod(op, field)
                    //criteria."$op"(field)
                    continue
                }
            }
        }
        //I think we should not blow up an error if some field isnt in domain, just add message to log
        log.info "MangoBuilder applyField domain ${getTargetClass(criteria).name} doesnt contains field $field"

    }

    static <D> DetachedCriteria<D> between(DetachedCriteria<D> criteria, String propertyName, List params) {
        List p = toType(criteria, propertyName, params) as List
        return criteria.between(propertyName, p[0], p[1])
    }

    static DetachedCriteria order(DetachedCriteria criteria, Object sort) {
        if (sort instanceof String) return criteria.order(sort as String)
        DetachedCriteria result
        (sort as Map).each { k, v ->
            result = criteria.order(k.toString(), v.toString())
        }
        return result
    }

    //@CompileDynamic
    static DetachedCriteria quickSearch(DetachedCriteria criteria, String value) {
        if(QueryMangoEntity.isAssignableFrom(getTargetClass(criteria))){
            Map<String, String> orMap = getQuickSearchFields(criteria).collectEntries {
                [(it.toString()): (criteria.persistentEntity.getPropertyByName(it).type == String ? value + "%" : value)]
            }
            def criteriaMap = ['$or': orMap] as Map<String, Object>
            return applyMap(criteria, MangoTidyMap.tidy(criteriaMap))
        }

    }

    @CompileDynamic //dynamic so we can access the protected targetClass
    static Class getTargetClass(DetachedCriteria criteria) {
        criteria.targetClass
    }

    @CompileDynamic //dynamic so we can access the protected targetClass.quickSearchFields
    static List<String>  getQuickSearchFields(DetachedCriteria criteria) {
        criteria.targetClass.quickSearchFields
    }

    @CompileDynamic
    static DetachedCriteria notIn(DetachedCriteria criteria, String propertyName, List params) {
        Map val = [:]
        val[propertyName] = ['$in': params]
        DetachedCriteria builtCrit = build(getTargetClass(criteria), val)
        return criteria.notIn(propertyName, builtCrit."$propertyName" as QueryableCriteria)
    }

    /**
     * Handles a conjunction
     * @param list junctions list of condition maps
     * @return This criterion
     */
    static DetachedCriteria and(DetachedCriteria criteria, List andList) {
        criteria.junctions << new Query.Conjunction()
        handleJunction(criteria, andList)
        return criteria
    }

    /**
     * Handles a disjunction
     * @param list junctions list of condition maps
     * @return This criterion
     */
    static DetachedCriteria or(DetachedCriteria criteria, List orList) {
        criteria.junctions << new Query.Disjunction()
        handleJunction(criteria, orList)
        return criteria
    }

    /**
     * Handles a disjunction
     * @param list junctions list of condition maps
     * @return This criterion
     */
    static DetachedCriteria not(DetachedCriteria criteria, List notList) {
        criteria.junctions << new Query.Negation()
        handleJunction(criteria, notList)
        return criteria
    }

    /**
     * junctions are basically used like a stack that we pop. when they finish they get added to the criteria list.
     * The add method checks to see if there is an active junction we are in.
     * @param mangoMap
     */
    static void handleJunction(DetachedCriteria criteria, List list) {
        try {
            applyMapOrList(criteria, list)
        }
        finally {
            Query.Junction lastJunction = criteria.junctions.remove(criteria.junctions.size() - 1)
            criteria.add lastJunction
        }
    }

    static Object toType(DetachedCriteria criteria, String propertyName, Object value) {
        if (value instanceof List) {
            return value.collect { toType(criteria, propertyName, it) }
        }
        PersistentProperty prop = criteria.getPersistentEntity().getPropertyByName(propertyName)
        Class typeToConvertTo = prop?.getType()

        Object valueToAssign = value

        if (valueToAssign instanceof String) {
            if (String.isAssignableFrom(typeToConvertTo)) {
                valueToAssign = value
            } else if (Number.isAssignableFrom(typeToConvertTo)) {
                valueToAssign = (value as String).asType(typeToConvertTo)
            } else if (Date.isAssignableFrom(typeToConvertTo)) {
                valueToAssign = IsoDateUtil.parse(value as String)
            }
        } else {
            valueToAssign = valueToAssign.asType(typeToConvertTo)
        }

        valueToAssign
    }
}
