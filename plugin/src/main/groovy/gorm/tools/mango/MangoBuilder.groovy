package gorm.tools.mango

import gorm.tools.beans.DateUtil
import grails.gorm.DetachedCriteria
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.query.criteria.DetachedAssociationCriteria
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.Criteria
import org.grails.datastore.mapping.query.api.QueryableCriteria

//@CompileStatic
class MangoBuilder {
    //DetachedCriteria criteria

    static final Map<String, String> compareOps = [
        '$gt'     : 'gt',
        '$eq'     : 'eq',
        '$gte'    : 'ge',
        '$lt'     : 'lt',
        '$lte'    : 'le',
        '$ne'     : 'ne',
        '$not'    : 'not',
        '$ilike'  : 'ilike',
        '$like'   : 'like',
        '$in'     : 'in',
        '$inList' : 'inList',
        '$gtf'    : 'gtProperty',
        '$gtef'   : 'geProperty',
        '$ltf'    : 'ltProperty',
        '$ltef'   : 'leProperty',
        '$eqf'    : 'eqProperty',
        '$nef'    : 'neProperty'
    ]

    static final Map<String, String> overrideOps = [
        '$between': 'between',
        '$nin'    : 'notIn'
    ]

    static final Map<String, String> junctionOps = [
        '$and': 'and',
        '$or' : 'or',
        '$not': 'not'
    ]

    static final Map<String, String> existOps = [
        '$isNull'   : 'isNull',
        '$isNotNull': 'isNotNull'
    ]

    static final Map<String, String> quickSearchOps = [
        '$quickSearch': 'quickSearch',
        '$q'          : 'quickSearch'
    ]

    static DetachedCriteria build(Class clazz, Map mangoMap, Closure callable = null) {
        DetachedCriteria detachedCriteria = new DetachedCriteria(clazz)
        return build(detachedCriteria, mangoMap, callable)
    }

    static DetachedCriteria build(DetachedCriteria criteria, Map mangoMap, Closure callable = null) {
        DetachedCriteria newCriteria = criteria.clone()
        applyMapOrList(newCriteria, MangoTidyMap.tidy(mangoMap))
        if (callable) newCriteria.with callable
        return newCriteria
    }

    static void applyMapOrList(DetachedCriteria criteria, mapOrList) {
        if (mapOrList instanceof Map) {
            applyMap(criteria, mapOrList)
        } else if (mapOrList instanceof List<Map>) {
            for (Map item : mapOrList) {
                applyMap(criteria, item)
            }
        } else {
            //pitch an error?
        }
    }

    /**
     * applies the map just like running a closure.call on this.
     * @param mangoMap
     */
    static void applyMap(DetachedCriteria criteria, Map mangoMap) {
        println "applyMap >>>>>>>>>>>>> $mangoMap"
        for (String key : mangoMap.keySet()) {
            String op = junctionOps[key]
            if (op) {
                //normalizer should have ensured all ops have a List for a value
                this."$op"(criteria, (List) mangoMap[key])
                continue
            } else { //it must be a field then
                applyField(criteria, key, mangoMap[key])
            }
        }
    }

    static void applyField(DetachedCriteria criteria, String field, Object fieldVal) {
        String qs = quickSearchOps[field]
        if (qs) {
            this."$qs"(criteria, fieldVal)
            return
        }
        if(field.matches(/.*[^.]Id/) && criteria.persistentEntity.properties.persistentPropertyNames.contains(field.replaceAll("Id\$", ""))){
            applyField(criteria, field.replaceAll("Id\$", ""), ['id': fieldVal])
        }
        else if (fieldVal instanceof String || fieldVal instanceof Number || fieldVal instanceof Boolean) {
            //TODO check if its a date field and parse
            criteria.eq(field, fieldVal)
        } else if (fieldVal instanceof Map) { // could be field=name fieldVal=['$like': 'foo%']
            //could be 1 or more too
            //for example field=amount and fieldVal=['$lt': 100, '$gt':200]
            Boolean isAssoc = (criteria.getPersistentEntity().getPropertyByName(field) instanceof Association)
            if(isAssoc){
                criteria."${field}"({
                    //println "$criteria -> $delegate -> $field -> $fieldVal"
                    this.applyMapOrList(delegate, fieldVal)
                    //this.applyMap(criteria, fieldVal.collectEntries { Map res = [:]; res["$field.${it.key}"] = it.value; res })
                })
            }else {
                for (String key : (fieldVal as Map).keySet()) {
                    //everything has to either be either a junction op or condition
                    String junctionOp = junctionOps[key]
                    def opArg = fieldVal[key]

                    if (junctionOp) {
                        //normalizer should have ensured all ops have a List for a value
                        this."$junctionOp"(criteria, (List) opArg)
                        continue
                    }

                    String ovops = overrideOps[key]
                    if (ovops) {
                        this."$ovops"(criteria, field, toType(criteria, field, opArg))
                        continue
                    }

                    String cond = compareOps[key]
                    if (cond) {
                        criteria."$cond"(field, toType(criteria, field, opArg))
                        continue
                    }

                    String ex = existOps[key]
                    if (ex) {
                        criteria."$ex"(field)
                        continue
                    }
                    //consider it a property then and we may be looking at this field=customer and fieldVal=['num': 100, 'name':'foo']
                    //missing method creates alias for nested property, but if key doesnt contain it looks in "parent" domain

                }
            }
        }
    }

    static DetachedCriteria between(DetachedCriteria criteria, String propertyName, List params) {
        //  List p = toType(criteria, propertyName, params)
        return criteria.between(propertyName, params[0], params[1])
    }

    static DetachedCriteria quickSearch(DetachedCriteria criteria, String value) {
        Map result = MangoTidyMap.tidy(['$or': criteria.targetClass.quickSearchFields.collectEntries {
            ["$it": value] //TODO: probably should check type and add `%` for strings
        }])

        return applyMap(criteria, result)
    }

    static DetachedCriteria notIn(DetachedCriteria criteria, String propertyName, List params) {
        Map val = [:]
        val[propertyName] = ['$in': params]
        return criteria.notIn(propertyName, ( MangoBuilder.build(criteria.targetClass, val)."$propertyName") as QueryableCriteria)
        //return criteria.notIn(propertyName, (new DetachedCriteria(criteria.targetClass).build(val)."$propertyName") as QueryableCriteria)
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
        def prop = criteria.getPersistentEntity().getPropertyByName(propertyName)
//        if(criteria instanceof DetachedAssociationCriteria){
//            def detCriteria = (DetachedAssociationCriteria)criteria
//            println "${detCriteria.association}"
//        }
//        println "$criteria -> $prop -> $propertyName -> $value"

        if (value instanceof String && prop){
            return value
        }

        def valueToAssign = value

        if (value instanceof List){
            return value.collect{toType(criteria, propertyName, it)}
        }

        if (value instanceof String && !criteria.persistentEntity.getPropertyByName(propertyName)) {
            Class typeToConvertTo = prop.getType()
            if (Number.isAssignableFrom(typeToConvertTo)) {
                valueToAssign = (value as String).asType(typeToConvertTo)
            } else if (Date.isAssignableFrom(typeToConvertTo)) {
                valueToAssign = DateUtil.parseJsonDate(value as String)
            }
        } else {
            valueToAssign = valueToAssign.asType(prop.type)
        }

        valueToAssign
    }
}
