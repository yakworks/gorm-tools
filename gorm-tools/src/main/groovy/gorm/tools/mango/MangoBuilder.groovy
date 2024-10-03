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
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.databinding.EntityMapBinder
import gorm.tools.mango.api.QueryArgs
import grails.gorm.DetachedCriteria
import yakworks.commons.lang.ClassUtils
import yakworks.commons.lang.EnumUtils
import yakworks.commons.model.IdEnum
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.IncludesKey

import static gorm.tools.mango.MangoOps.CompareOp
import static gorm.tools.mango.MangoOps.ExistOp
import static gorm.tools.mango.MangoOps.JunctionOp
import static gorm.tools.mango.MangoOps.OverrideOp
import static gorm.tools.mango.MangoOps.PropertyOp
import static gorm.tools.mango.MangoOps.QSEARCH
import static gorm.tools.mango.MangoOps.SORT
import static gorm.tools.mango.MangoOps.SubQueryOp

/**
 * the main builder to turn Mango QL Maps or json into DetachedCriteria for Gorm
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@SuppressWarnings(['FieldName', 'ConfusingMethodName']) //codenarc doesn't like the names we use to make this builder clean
@CompileStatic
@Slf4j
class MangoBuilder {

    @Autowired IncludesConfig includesConfig

    @CompileDynamic //dynamic so it can access the protected criteria.clone
    static <D> MangoDetachedCriteria<D> cloneCriteria(DetachedCriteria<D> criteria) {
        (MangoDetachedCriteria)criteria.clone()
    }

    public <D> MangoDetachedCriteria<D> build(Class<D> clazz, QueryArgs qargs, @DelegatesTo(MangoDetachedCriteria) Closure callable = null) {
        MangoDetachedCriteria<D> mangoCriteria = createCriteria(clazz, qargs, callable)
        applyCriteria(mangoCriteria)
        return mangoCriteria
    }

    /**
     * Creates the MangoDetachedCriteria object with the queryArgs.
     * Does NOT apply or set it up yet.
     */
    <D> MangoDetachedCriteria<D> createCriteria(Class<D> clazz, QueryArgs queryArgs, Closure applyClosure){
        MangoDetachedCriteria<D> mangoCriteria = new MangoDetachedCriteria<D>(clazz)
        //assign the queryArgs for use later if needed
        mangoCriteria.queryArgs = queryArgs
        //assign the the criteriaMap and run the tidy on it to normalize it.
        Map criteria = queryArgs.buildCriteriaMap()
        //normalize the map and assign it
        mangoCriteria.criteriaMap = MangoTidyMap.tidy(criteria)
        mangoCriteria.criteriaClosure = applyClosure
        return mangoCriteria
    }

    /**
     * Applies the criteriaMap and criteriaClosure to setup the MangoDetachedCriteria
     */
    <D> MangoDetachedCriteria<D> applyCriteria(MangoDetachedCriteria<D> mangoCriteria){
        QueryArgs qargs = mangoCriteria.queryArgs
        //will be copy if sort exists
        Map tidyMap = mangoCriteria.criteriaMap
        Closure applyClosure = mangoCriteria.criteriaClosure

        //apply the map
        applyMapOrList(mangoCriteria, tidyMap)

        //apply the closure
        if (applyClosure) {
            final Closure clonedClosure = (Closure) applyClosure.clone()
            clonedClosure.setResolveStrategy(Closure.DELEGATE_FIRST)
            mangoCriteria.with(clonedClosure)
        }
        //
        //$sort was probably on the criteria as its added in QueryArgs?? but if not then use the property
        if(qargs.sort && !tidyMap.containsKey(SORT)){
            order(mangoCriteria, qargs.sort)
        }

        if(qargs.projections){
            applyProjections(mangoCriteria, qargs.projections)
        }

        //apply select properties, this should not
        if(qargs.select){
            applySelect(mangoCriteria, qargs.select)
        }

        if(qargs.timeout) mangoCriteria.setTimeout(qargs.timeout)

        return mangoCriteria
    }

    /**
     * calls list for the criteria, if criteria has projections then calls mapList
     * which uses JpqlQueryBuilder
     */
    static List list(MangoDetachedCriteria criteria, Map args) {
        List resList
        if(criteria.projections){
            resList =  criteria.mapList(args)
        } else {
            //return standard list
            resList =  criteria.list(args)
        }
        return resList
    }

    /**
     * Apply projections from map in form [key:type] where type is sum, group, count, min, max or avg
     *
     * @param criteria the criteria to apply the project
     * @param projs the map of projections to apply
     */
    void applyProjections(MangoDetachedCriteria criteria, Map projs) {
        //assume its a map
        (projs as Map<String,String>).each { String k, String v ->
            if(v == 'sum'){
                criteria.sum(k)
            } else if (v == 'group'){
                criteria.groupBy(k)
            } else if (v == 'avg'){
                criteria.avg(k)
            } else if (v == 'count'){
                criteria.countDistinct(k)
            } else if (v == 'min'){
                criteria.min(k)
            } else if (v == 'max'){
                criteria.max(k)
            }
        }
    }

    /**
     * Apply projections from map in form [key:type] where type is sum, group, count, min, max or avg
     *
     * @param criteria the criteria to apply the project
     * @param projs the map of projections to apply
     */
    void applySelect(MangoDetachedCriteria criteria, List<String> projs) {
        for(String prop : projs){
            criteria.property(prop)
        }
    }

    void applyMapOrList(DetachedCriteria criteria, Object mapOrList) {
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
     */
    void applyMap(DetachedCriteria criteria, Map mangoMap) {
        log.debug "applyMap $mangoMap"
        for (String key : mangoMap.keySet()) {
            def val = mangoMap[key]

            if(key == SORT) {
                order(criteria, val)
                continue
            }

            if(key == QSEARCH ) {
                qSearch(criteria, val)
                continue
            }

            JunctionOp jop = EnumUtils.getEnum(JunctionOp, key)
            if (jop) {
                //tidyMap should have ensured all ops have a List for a value
                // or if its an instance of
                invoke(jop.op, criteria, (List) val)
                continue
            }

            //subquery, value should be preprocessed and be a QueryableCriteria
            //exists is one example we are using here
            SubQueryOp subOp = EnumUtils.getEnum(SubQueryOp, key)
            if (subOp && val instanceof QueryableCriteria) {
                //invoke(subOp.op, (QueryableCriteria) val)
                InvokerHelper.invokeMethod(criteria, subOp.op, val)
                continue
            }

            //it must be a field then
            applyField(criteria, key, mangoMap[key])
        }
    }

    Object invoke(String op, Object... args) {
        InvokerHelper.invokeMethod(this, op, args)
    }

    void applyField(DetachedCriteria criteria, String field, Object fieldVal) {

        PersistentProperty prop = criteria.persistentEntity.getPropertyByName(field)

        //if its an association then call it as a method so methodmissing will pick it up and build the DetachedAssocationCriteria
        if (prop instanceof Association) {
            //if its its map and valid op $eq then assume its an object compare and just do it right away
            if(fieldVal instanceof Map){
                //if the fieldVal has a key like $eq then us it, when comparing objects
                def firstKey = (fieldVal as Map).entrySet()[0].key as String
                if(MangoOps.isValidOp(firstKey)){
                    applyFieldMap(criteria, field, fieldVal)
                    return
                }
            }

            criteria.invokeMethod(field){
                //the delegate is the DetachedAssocationCriteria. See methodMissing in AbstractDetachedCriteria
                applyMapOrList((DetachedCriteria) delegate, fieldVal)
                return
            }
        }
        // ID Helper, if field ends in Id then try removing the Id postfix and see if its a property
        else if (!prop && field.matches(/.*[^.]Id/) && criteria.persistentEntity.getPropertyByName(field.replaceAll("Id\$", ""))) {
            applyFieldMap(criteria, field.replaceAll("Id\$", ".id"), fieldVal as Map)
            //applyField(criteria, field.replaceAll("Id\$", ".id"), fieldVal)
        }
        else if (!(fieldVal instanceof Map) && !(fieldVal instanceof List && prop != null)) {
            criteria.eq(field, toType(criteria, field, fieldVal))
        }
        //ENUMS
        else if (prop && IdEnum.isAssignableFrom(prop.type) && fieldVal instanceof Map && fieldVal.containsKey('id')) {
            //&& fieldVal instanceof Map && fieldVal.containsKey('id')
            applyField(criteria, field, fieldVal['id'])
        }
        //its common a MangoOp, for example fieldVal=['$eq': 'foo']
        else if (fieldVal instanceof Map) {
            applyFieldMap(criteria, field, fieldVal)
        }
        else { //will get here if fieldVal is not a Map and prop is null. maybe only blow error if prop is null?
            //fieldVal is bad somehow so blow an error
            String msg = "No Match [field:$field, entity:${getTargetClass(criteria).name}, fieldVal: $fieldVal]"
            //XXX log it out for now, remove this once we know its ok
            log.error(msg)
            throw new IllegalArgumentException(msg)
        }

    }

    // field=name fieldVal=['$like': 'foo%']
    void applyFieldMap(DetachedCriteria criteria, String field, Map fieldVal) {
        //could be 1 or more too
        //for example field=amount and fieldVal=['$lt': 100, '$gt':200]
        for (String key : (fieldVal as Map).keySet()) {
            //everything has to either be either a junction op or condition
            Object opArg = fieldVal[key]

            MangoOps.JunctionOp jop = EnumUtils.getEnum(JunctionOp, key)
            if (jop) {
                //normalizer should have ensured all ops have a List for a value
                invoke(jop.op, criteria, (List) opArg)
                continue
            }

            MangoOps.OverrideOp oop = EnumUtils.getEnum(OverrideOp, key)
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
            } else {
                //field is not an association, but value is a map and key doesnt match any of the operator ?
                //It is invalid query example ("Source.sourceId = "xx"), case matters so wont find Source
                throw new IllegalArgumentException("Invalid criteria for field:$field value:$fieldVal")
            }

        }
    }

    public <D> DetachedCriteria<D> between(DetachedCriteria<D> criteria, String propertyName, List params) {
        List p = toType(criteria, propertyName, params) as List
        return criteria.between(propertyName, p[0], p[1])
    }

    DetachedCriteria order(DetachedCriteria criteria, Object sort) {
        DetachedCriteria result
        Map<String,String> sortMap

        if (sort instanceof String){
            sortMap = [(sort): 'asc'] as Map<String,String>
        } else {
            sortMap = sort as Map<String,String>
        }

        //assume its a map
        sortMap.each { k, v ->
            criteria.order(k.toString(), v.toString())
        }
        return result
    }

    /**
     * simple overrride so we can return DetachedCriteria and chain
     */
    DetachedCriteria createAlias(DetachedCriteria criteria, String associationPath, String alias) {
        criteria.createAlias(associationPath, alias) as DetachedCriteria
    }

    DetachedCriteria qSearch(DetachedCriteria criteria, Object val) {
        List<String> qSearchFields
        String qText
        // if its a map then assume its already setup with text and fields to search on
        if(val instanceof Map){
            qText = val['text'] as String
            qSearchFields = val['fields'] as List<String>
        }
        //otherwise we assume its a string and find the QSearchFields
        else if (val instanceof String){
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

    @CompileDynamic //dynamic so we can access the protected targetClass
    Class getTargetClass(DetachedCriteria criteria) {
        criteria.targetClass
    }

    List<String> getQSearchFields(DetachedCriteria criteria) {
        Class entityClazz = getTargetClass(criteria)
        return includesConfig.getIncludes(entityClazz, IncludesKey.qSearch.name())
    }

    @CompileDynamic
    DetachedCriteria notIn(DetachedCriteria criteria, String propertyName, List params) {
        Map val = [:]
        val.put(propertyName, ['$in': params])
        // DetachedCriteria builtCrit = build(getTargetClass(criteria), val)
        // def qryCrit = builtCrit[propertyName] as QueryableCriteria
        // return criteria.notIn(propertyName, qryCrit)
        return not(criteria, [val])
    }

    /**
     * Handles a conjunction
     * @param list junctions list of condition maps
     * @return This criterion
     */
    DetachedCriteria and(DetachedCriteria criteria, List andList) {
        getJunctions(criteria) << new Query.Conjunction()
        handleJunction(criteria, andList)
        return criteria
    }

    /**
     * Handles a disjunction
     * @param list junctions list of condition maps
     * @return This criterion
     */
    DetachedCriteria or(DetachedCriteria criteria, List orList) {
        getJunctions(criteria) << new Query.Disjunction()
        handleJunction(criteria, orList)
        return criteria
    }

    /**
     * Handles a disjunction
     * @param list junctions list of condition maps
     * @return This criterion
     */
    DetachedCriteria not(DetachedCriteria criteria, List notList) {
        getJunctions(criteria) << new Query.Negation()
        handleJunction(criteria, notList)
        return criteria
    }

    /**
     * junctions are basically used like a stack that we pop. when they finish they get added to the criteria list.
     * The add method checks to see if there is an active junction we are in.
     */
    void handleJunction(DetachedCriteria criteria, List list) {
        try {
            applyMapOrList(criteria, list)
        }
        finally {
            var junctions = getJunctions(criteria)
            Query.Junction lastJunction = junctions.remove(junctions.size() - 1)
            criteria.add lastJunction
        }
    }

    Object toType(DetachedCriteria criteria, String propertyName, Object value) {
        if (value instanceof List) {
            return value.collect { toType(criteria, propertyName, it) }
        }
        PersistentProperty prop = criteria.getPersistentEntity().getPropertyByName(propertyName)
        Class typeToConvertTo = prop?.getType() as Class

        //if typeToConvertTo is null then return just return obj
        if(!typeToConvertTo) {
            if(value instanceof Integer || (value instanceof String && value.isLong())){
                //almost always long so cast it do we dont get weird cast error
                return value as Long
            }
            return value
        }

        Object v = value

        if (v instanceof String) {
            Object parsedVal = EntityMapBinder.parseBasicType(v, typeToConvertTo)
            if (!(parsedVal instanceof EntityMapBinder.UnParsed)) {
                v = parsedVal
            }
            else if (typeToConvertTo.isEnum()) {
                v = EnumUtils.getEnum(typeToConvertTo, (String)v)
            }
            else if (UUID.isAssignableFrom(typeToConvertTo)) {
                v = UUID.fromString((String)v)
            }

        }
        else if (typeToConvertTo?.isEnum() && (v instanceof Number || v instanceof Map)){
            def idVal = v //assume its a number
            if(v instanceof Map) idVal = v['id']
            v = getEnumWithGet(typeToConvertTo, idVal as Number)
        }
        else {
            v = v.asType(typeToConvertTo)
        }

        return v
    }

    static getEnumWithGet(Class<?> enumClass, Number id){
        return ClassUtils.callStaticMethod(enumClass, 'get', id)
    }

    @CompileDynamic
    List<Query.Junction> getJunctions(DetachedCriteria criteria){
        criteria.@junctions
    }
}
