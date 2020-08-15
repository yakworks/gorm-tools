/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.commons.lang3.EnumUtils
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
@SuppressWarnings(['FieldName', 'ConfusingMethodName']) //codenarc doesn't like the names we use to make this builder clean
@CompileStatic
@Slf4j
class MangoBuilder {

    static final String SORT = '$sort'
    static final String Q = '$q'
    static final String QSEARCH = '$qSearch'

    @CompileStatic
    enum CompareOp {
        $gt, $eq, $gte, $lt, $lte, $ne, $not, $ilike, $like, $in, $inList

        private final String op //private for security
        String getOp(){ return op }

        CompareOp() {
            this.op = name().substring(1) //remove $
        }
    }

    @CompileStatic
    enum PropertyOp {
        $gtf('gtProperty'),
        $gtef('geProperty'),
        $ltf('ltProperty'),
        $ltef('leProperty'),
        $eqf('eqProperty'),
        $nef('neProperty')

        private final String op //private for security
        String getOp(){ return op }

        PropertyOp(String op) {
            this.op = op
        }
    }

    @CompileStatic
    enum OverrideOp {
        $between('between'),
        $nin('notIn')

        private final String op
        String getOp(){ return op }

        OverrideOp(String op) {
            this.op = op
        }
    }

    @CompileStatic
    enum JunctionOp {
        $and, $or, $not

        private final String op //private for security
        String getOp(){ return op }

        JunctionOp() {
            this.op = name().substring(1) //remove $
        }
    }

    @CompileStatic
    enum ExistOp {
        $isNull, $isNotNull

        private final String op
        String getOp(){ return op }

        ExistOp() {
            this.op = name().substring(1) //remove $
        }
    }

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
            def val = mangoMap[key]

            if(key == SORT) {
                order(criteria, val)
                continue
            }

            if(key == QSEARCH || key == Q) {
                qSearch(criteria, val)
                continue
            }

            JunctionOp jop = EnumUtils.getEnum(JunctionOp, key)
            if (jop) {
                //tidyMap should have ensured all ops have a List for a value
                invoke(jop.op, criteria, (List) val)
                continue
            }

            //it must be a field then
            applyField(criteria, key, mangoMap[key])
        }
    }

    static Object invoke(String op, Object... args) {
        InvokerHelper.invokeStaticMethod(MangoBuilder, op, args)
    }

    static void applyField(DetachedCriteria criteria, String field, Object fieldVal) {

        PersistentProperty prop = criteria.persistentEntity.getPropertyByName(field)

        //if its an association then call it as a method so methodmissing will pick it up and build the DetachedAssocationCriteria
        if (prop instanceof Association) {
            criteria.invokeMethod(field){
                //the delegate is the DetachedAssocationCriteria. See methodMissing in AbstractDetachedCriteria
                applyMapOrList((DetachedCriteria) delegate, fieldVal)
                return
            }
        }
        // if field ends in Id then try removing the Id postfix and see if its a property
        else if (field.matches(/.*[^.]Id/) && criteria.persistentEntity.getPropertyByName(field.replaceAll("Id\$", ""))) {
            applyField(criteria, field.replaceAll("Id\$", ""), ['id': fieldVal])
        } else if (!(fieldVal instanceof Map) && !(fieldVal instanceof List && prop != null)) {
            criteria.eq(field, toType(criteria, field, fieldVal))
        } else if (fieldVal instanceof Map && prop) { // could be field=name fieldVal=['$like': 'foo%']
            //could be 1 or more too
            //for example field=amount and fieldVal=['$lt': 100, '$gt':200]
            for (String key : (fieldVal as Map).keySet()) {
                //everything has to either be either a junction op or condition
                Object opArg = fieldVal[key]

                JunctionOp jop = EnumUtils.getEnum(JunctionOp, key)
                if (jop) {
                    //normalizer should have ensured all ops have a List for a value
                    invoke(jop.op, criteria, (List) opArg)
                    continue
                }

                OverrideOp oop = EnumUtils.getEnum(OverrideOp, key)
                if (oop) {
                    invoke(oop.op, criteria, field, toType(criteria, field, opArg))
                    continue
                }

                CompareOp cop = EnumUtils.getEnum(CompareOp, key) // CompareOp.valueOf(key)
                if (cop) {
                    if (opArg == null) {
                        criteria.isNull(field)
                        continue
                    }
                    criteria.invokeMethod(cop.op, [field, toType(criteria, field, opArg)])
                    continue
                }

                PropertyOp pop = EnumUtils.getEnum(PropertyOp, key)
                if (pop) {
                    criteria.invokeMethod(pop.op, [field, opArg])
                    continue
                }

                ExistOp eop = EnumUtils.getEnum(ExistOp, key)
                if (eop) {
                    criteria.invokeMethod(eop.op, field)
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

    static DetachedCriteria qSearch(DetachedCriteria criteria, Object val) {
        List<String> qSearchFields
        String qText
        // if its a map then assume its got the text and fields to search on
        if(val instanceof Map){
            qText = val['text'] as String
            qSearchFields = val['fields'] as List<String>
        } else if (QueryMangoEntity.isAssignableFrom(getTargetClass(criteria))){
            qText = val as String
            qSearchFields = getQSearchFields(criteria)
        }

        if(qSearchFields) {
            Map<String, String> orMap = qSearchFields.collectEntries {
                [(it.toString()): (criteria.persistentEntity.getPropertyByName(it).type == String ? qText + '%' : qText)]
            }
            def criteriaMap = ['$or': orMap] as Map<String, Object>
            // println "criteriaMap $criteriaMap"
            return applyMap(criteria, MangoTidyMap.tidy(criteriaMap))
        }
    }

    static DetachedCriteria qSearchold(DetachedCriteria criteria, String value) {
        if(QueryMangoEntity.isAssignableFrom(getTargetClass(criteria))){
            Map<String, String> orMap = getQSearchFields(criteria).collectEntries {
                [(it.toString()): (criteria.persistentEntity.getPropertyByName(it).type == String ? value + "%" : value)]
            }
            def criteriaMap = ['$or': orMap] as Map<String, Object>
            println "criteriaMap $criteriaMap"
            return applyMap(criteria, MangoTidyMap.tidy(criteriaMap))
        }

    }

    @CompileDynamic //dynamic so we can access the protected targetClass
    static Class getTargetClass(DetachedCriteria criteria) {
        criteria.targetClass
    }

    @CompileDynamic //dynamic so we can access the protected targetClass.quickSearchFields
    static List<String>  getQSearchFields(DetachedCriteria criteria) {
        criteria.targetClass.qSearchFields
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
        Class typeToConvertTo = prop?.getType() as Class

        Object v = value

        if (v instanceof String) {
            if (String.isAssignableFrom(typeToConvertTo)) {
                v = value
            } else if (Number.isAssignableFrom(typeToConvertTo)) {
                v = (value as String).asType(typeToConvertTo as Class<Number>)
            } else if (Date.isAssignableFrom(typeToConvertTo)) {
                v = IsoDateUtil.parse(value as String)
            } else if (typeToConvertTo.isEnum()) {
                v = getEnum(typeToConvertTo, v)
            }
        }
        else if (typeToConvertTo.isEnum() && v instanceof Number){
            v = getEnumWithGet(typeToConvertTo, v)
        }
        else {
            v = v.asType(typeToConvertTo)
        }

        return v
    }

    //FIXME clean this up so its a compile static
    @CompileDynamic
    static def getEnum(Class typeToConvertTo, Object val){
       return EnumUtils.getEnum(typeToConvertTo, val)
    }

    //FIXME clean this up so its a compile static
    @CompileDynamic
    static def getEnumWithGet(Class<?> enumClass, Number id){
        //See the repoEvents code, we can use ReflectionUtils and cache the the get method, then use CompileStatic
        return enumClass.get(id)
    }
}
