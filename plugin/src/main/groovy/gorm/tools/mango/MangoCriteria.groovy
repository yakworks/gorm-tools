package gorm.tools.mango

import grails.gorm.DetachedCriteria
import groovy.transform.CompileDynamic
import org.grails.datastore.gorm.query.criteria.AbstractDetachedCriteria
import org.grails.datastore.gorm.query.criteria.DetachedAssociationCriteria
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.Criteria

/**
 * Builds DetachedCriteria from a map. Everything essentially gets put into 3 lists; criteria, projections and orders.
 * can be seen in DynamicFinder.applyDetachedCriteria which is called in DetachedCriteria.withPopulatedQuery
 * @param <T>
 */
class MangoCriteria<T> extends DetachedCriteria<T>{

    static final Map<String,String> compareOps = [
        '$gt' : 'gt',
        '$eq' : 'eq',
        '$gte': 'ge',
        '$lt' : 'lt',
        '$lte': 'le',
        '$ilike': 'ilike',
        '$like': 'like'
    ]

    static final Map<String,String> junctionOps = [
        '$and': 'and',
        '$or': 'or'
    ]

    /**
     * Constructs a DetachedCriteria instance target the given class and alias for the name
     * @param targetClass The target class
     * @param alias The root alias to be used in queries
     */
    MangoCriteria(Class<T> targetClass, String alias = null) {
        super(targetClass, alias)
    }

    /**
     * Enable the builder syntax for constructing Criteria
     *
     * @param mangoMap The map with the mongo mango like language
     * @return A new criteria instance
     */
    DetachedCriteria<T> build(Map mangoMap) {
        println "build with $mangoMap"
        MangoCriteria<T> newCriteria = this.clone()
        newCriteria.applyMap(mangoMap)
        return newCriteria
    }

    @Override
    protected DetachedCriteria newInstance() {
        new MangoCriteria(targetClass, alias)
    }

    protected void applyMapOrList(mapOrList) {
        if(mapOrList instanceof Map){
            applyMap(mapOrList)
        }
        else if (mapOrList instanceof List){
            for (Object item : mapOrList) {
                applyMap(item)
            }
        } else{
            //pitch an error?
        }
    }

    /**
     * applies the map just like running a closure.call on this.
     * @param mangoMap
     */
    protected void applyMap(Map mangoMap) {
        for (String key : mangoMap.keySet()) {
            String op = compareOps[key]
            if(op){
                //normalizer should have ensured all ops have a List for a value
                "$op"((List) mangoMap[key])
                continue
            }
            else { //it must be a field then
                applyField( key, mangoMap[key])
            }
        }
    }

    protected void applyField(String field, Object fieldVal) {
        if(fieldVal instanceof String || fieldVal instanceof Number || fieldVal instanceof Boolean){
            //TODO check if its a date field and parse
            eq(field,fieldVal)
        }
        else if(fieldVal instanceof Map) { // could be field=name fieldVal=['$like': 'foo%']
            //could be 1 or more too
            //for example field=amount and fieldVal=['$lt': 100, '$gt':200]
            for (String key : fieldVal.keySet()) {
                //everything has to either be either a junction op or condition
                String op = junctionOps[key]
                def opArg = fieldVal[key]

                if(op){
                    //normalizer should have ensured all ops have a List for a value
                    "$op"((List) opArg)
                    continue
                }
                String cond = compareOps[key]
                if(cond){
                    "$cond"(field, opArg)
                    continue
                }
                //consider it a property then and we may be looking at this field=customer and fieldVal=['num': 100, 'name':'foo']
                "$field"({applyMap(fieldVal as Map)})
            }
        }

    }

    /**
     * Handles a conjunction
     * @param list junctions list of condition maps
     * @return This criterion
     */
    MangoCriteria<T> and( List andList ) {
        junctions << new Query.Conjunction()
        handleJunction(andList)
        return this
    }

    /**
     * Handles a disjunction
     * @param list junctions list of condition maps
     * @return This criterion
     */
    MangoCriteria<T> or( List orList ) {
        junctions << new Query.Disjunction()
        handleJunction(orList)
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
            def lastJunction = junctions.remove(junctions.size() - 1)
            add lastJunction
        }
    }
}
