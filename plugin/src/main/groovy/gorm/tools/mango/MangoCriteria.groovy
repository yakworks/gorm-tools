package gorm.tools.mango

import gorm.tools.GormMetaUtils
import gorm.tools.beans.DateUtil
import grails.core.GrailsDomainClass
import grails.gorm.DetachedCriteria
import groovy.transform.CompileDynamic
import org.grails.datastore.gorm.finders.FinderMethod
import org.grails.datastore.gorm.query.criteria.AbstractDetachedCriteria
import org.grails.datastore.gorm.query.criteria.DetachedAssociationCriteria
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.Criteria
import org.grails.datastore.mapping.query.api.QueryableCriteria


/**
 * Builds DetachedCriteria from a map. Everything essentially gets put into 3 lists; criteria, projections and orders.
 * can be seen in DynamicFinder.applyDetachedCriteria which is called in DetachedCriteria.withPopulatedQuery
 * @param < T >
 */
class MangoCriteria<T> extends DetachedCriteria<T> {

    protected GrailsDomainClass domainClass
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
            '$nin'    : 'notIn',
            '$between': 'between',
            '$gtf'    : 'gtProperty',
            '$gtef'   : 'geProperty',
            '$ltf'    : 'ltProperty',
            '$ltef'   : 'leProperty',
            '$eqf'    : 'eqProperty',
            '$nef'    : 'neProperty'
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

    /**
     * Constructs a DetachedCriteria instance target the given class and alias for the name
     * @param targetClass The target class
     * @param alias The root alias to be used in queries
     */
    MangoCriteria(Class<T> targetClass, String alias = null) {
        super(targetClass, alias)
        domainClass = GormMetaUtils.findDomainClass(targetClass.name)
    }

    /**
     * Enable the builder syntax for constructing Criteria
     *
     * @param mangoMap The map with the mongo mango like language
     * @return A new criteria instance
     */
    DetachedCriteria<T> build(Map mangoMap, Closure callable = null) {
        MangoCriteria<T> newCriteria = this.clone()
        newCriteria.applyMap(mangoMap)
        if (callable) newCriteria.with callable
        return newCriteria
    }

    @Override
    protected DetachedCriteria newInstance() {
        new MangoCriteria(targetClass, alias)
    }


    protected void applyMapOrList(mapOrList) {
        if (mapOrList instanceof Map) {
            applyMap(mapOrList)
        } else if (mapOrList instanceof List) {
            for (Object item : mapOrList) {
                applyMap(item)
            }
        } else {
            //pitch an error?
        }
    }

    /**
     * applies the map just like running a closure.call on this.
     * @param mangoMap
     */
    protected void applyMap(Map mangoMap) {
        println "applyMap >>>>>>>>>>>>> $mangoMap"
        for (String key : mangoMap.keySet()) {
            String op = junctionOps[key]
            if (op) {
                //normalizer should have ensured all ops have a List for a value
                "$op"((List) mangoMap[key])
                continue
            } else { //it must be a field then
                applyField(key, mangoMap[key])
            }
        }
    }

    protected void applyField(String field, Object fieldVal) {
        String qs = quickSearchOps[field]
        if (qs) {
            "$qs"(fieldVal)
            return
        }
        if(field.matches(/.*[^.]Id/) && persistentEntity.properties.persistentPropertyNames.contains(field.replaceAll("Id\$", ""))){
            applyField(field.replaceAll("Id\$", ""), ['id': fieldVal])
        }
        else if (fieldVal instanceof String || fieldVal instanceof Number || fieldVal instanceof Boolean) {
            //TODO check if its a date field and parse
            eq(field, fieldVal)
        } else if (fieldVal instanceof Map) { // could be field=name fieldVal=['$like': 'foo%']
            //could be 1 or more too
            //for example field=amount and fieldVal=['$lt': 100, '$gt':200]
            for (String key : fieldVal.keySet()) {
                //everything has to either be either a junction op or condition
                String op = junctionOps[key]
                def opArg = fieldVal[key]

                if (op) {
                    //normalizer should have ensured all ops have a List for a value
                    "$op"((List) opArg)
                    continue
                }
                String cond = compareOps[key]
                if (cond) {
                    "$cond"(field, toType(field,opArg))
                    continue
                }

                String ex = existOps[key]
                if (ex) {
                    "$ex"(field)
                    continue
                }
                //consider it a property then and we may be looking at this field=customer and fieldVal=['num': 100, 'name':'foo']
                //missing method creates alias for nested property, but if key doesnt contain it looks in "parent" domain
                "${field}"(field, {
                    applyMap(fieldVal.collectEntries { Map res = [:]; res["$field.${it.key}"] = it.value; res })
                })
            }
        }
    }



    MangoCriteria<T> between(String propertyName, List params) {
        return (MangoCriteria<T>) super.between(propertyName, params[0], params[1])
    }

    MangoCriteria<T> quickSearch(String value) {
        Map result = MangoTidyMap.tidy(['$or': targetClass.quickSearchFields.collectEntries {
            ["$it": value] //TODO: probably should check type and add `%` for strings
        }])

        return (MangoCriteria<T>) applyMap(result)
    }

    MangoCriteria<T> notIn(String propertyName, List params) {
        Map val = [:]
        val[propertyName] = ['$in': params]
        return (MangoCriteria<T>) super.notIn(propertyName, (new MangoCriteria(targetClass).build(val)."$propertyName") as QueryableCriteria)
    }

    Object toType(String propertyName, Object value) {
        if (value instanceof String && domainClass.getPropertyByName(propertyName))
            return value
        def valueToAssign = value
        if (value instanceof List){
            return value.collect{toType(propertyName, it)}
        }
        def prop = domainClass.getPropertyByName(propertyName)
         if (value instanceof String && !domainClass.getPropertyByName(propertyName)) {
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

    /**
     * Handles a conjunction
     * @param list junctions list of condition maps
     * @return This criterion
     */
    MangoCriteria<T> and(List andList) {
        junctions << new Query.Conjunction()
        handleJunction(andList)
        return this
    }

    /**
     * Handles a disjunction
     * @param list junctions list of condition maps
     * @return This criterion
     */
    MangoCriteria<T> or(List orList) {
        junctions << new Query.Disjunction()
        handleJunction(orList)
        return this
    }

    /**
     * Handles a disjunction
     * @param list junctions list of condition maps
     * @return This criterion
     */
    Criteria not(List notList) {
        junctions << new Query.Negation()
        handleJunction(notList)
        return this
    }

    /**
     * junctions are basically used like a stack that we pop. when they finish they get added to the criteria list.
     * The add method checks to see if there is an active junction we are in.
     * @param mangoMap
     */
    protected void handleJunction(List list) {
        try {
            applyMapOrList(list)
        }
        finally {
            Query.Junction lastJunction = junctions.remove(junctions.size() - 1)
            add lastJunction
        }
    }
}
