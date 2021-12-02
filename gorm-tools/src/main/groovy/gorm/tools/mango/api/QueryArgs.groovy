/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api

import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import gorm.tools.beans.Pager
import yakworks.commons.map.Maps

import static gorm.tools.mango.MangoOps.CRITERIA

/**
 * Builder arguments for a query to pass from controllers etc to the MangoQuery
 * Can think of it a bit like a SQL query.
 * This holds
 *  - what we want to select (includes)
 *  - the where conditions (criteria)
 *  - the orderBy (sort)
 *  - and how we want the paging to be handled (pager), number of items per page, etc..
 *
 * contains some intermediary fields such as 'q' that are used to parse it into what we need
 */
@CompileStatic
class QueryArgs {

    static QueryArgs of(Pager pager){
        def qa = new QueryArgs()
        qa.pager = pager
        return qa
    }

    static QueryArgs of(Map params){
        def qa = new QueryArgs()
        return qa.build(params)
    }

    /**
     * Criteria map to pass to the MangoBuilder
     */
    Map<String, Object> criteria = [:] as Map<String, Object>

    /**
     * The Pager instance for paged list queries
     */
    Pager pager

    /**
     * If q is a string then these are the field to use to build the queryCriteria
     * Should get populated from the includes map thats in config or domain static
     */
    List<String> qSearchFields

    /**
     * holder for sort configuration to make it easier to grok
     * The key is the field, can be dot path for nested like foo.bar.baz
     * The value is either 'asc' or 'desc'
     */
    Map<String, String> sort

    /**
     * Setups the criteria and pager
     *
     * Translates q from json
     * example params= [q: "{foo: 'test*'}", sort:'foo', page: 2, offset: 10]
     * criteria= [foo:'test*', $sort:'foo'] and pager will be setup
     *
     * Transalates qSearch fields when string
     * params= [q: "foo", qSearchFields:['name', 'num']]
     * criteria= [$q: [text: "foo", 'fields': ['name', 'num']
     *
     * @param paramsMap the params to parse, this is clones and no changes will be made to it
     *
     * @return this instance
     */
    QueryArgs build(Map<String, ?> paramsMap){
        //copy it
        Map params = Maps.deepCopy(paramsMap) as Map<String, Object>


        //remove the fields that grails adds for controller and action
        params.removeAll {it.key in ['controller', 'action'] }

        // pull out the max, page and offset and assume the rest is criteria,
        // if pager is already set then we do nothing with the pagerMap
        Map pagerMap = [:]
        ['max', 'offset', 'page'].each{ String k ->
            if(params.containsKey(k)) pagerMap[k] = params.remove(k)
        }
        // if no pager was set then use what we just removed to set one up
        if(!pager) pager = new Pager(pagerMap)

        //pull out the sort and order if its there
        String sortField = params.remove('sort')
        String orderBy = params.remove('order') ?: 'asc'
        if(sortField) {
            sort = [(sortField): orderBy] as Map<String, String>
        }

        //jsonSlurper LAX allows fields to not be quoted
        JsonSlurper jsonSlurper = new JsonSlurper().setType(JsonParserType.LAX)

        // check for and remove the q param
        // whatever is in q if its parsed as a map and set to the criteria so it overrides everything
        def qProp = params.q ? params.remove('q') : params.remove(CRITERIA)

        if(qProp && qProp instanceof String) {
            String qString = qProp as String

            //if the q param start with { then assume its json and parse it
            //the parsed map will be set to the criteria.
            if (qString.trim().startsWith('{')) {
                // parseText returns LazyValueMap which will throw `Not that kind of map` when trying to add new key
                criteria = new HashMap(jsonSlurper.parseText(qString) as Map)
            }
            //if it doesn't start with { then its quick search so check for qSearchFields
            else if (qSearchFields) {
                //if it has a qsFields then set up the map
                Map qMap = ['text': qString, 'fields': qSearchFields]
                criteria['$q'] = qMap
            } else {
                criteria['$q'] = qString
            }
        }
        //if qprop exists and its a Map then its programatic execution and just set criteria to it
        else if(qProp && qProp instanceof Map){
            criteria = qProp as Map
        }
        //finally if no q was passed in then use whatever is left in params for the criteria
        else {
            criteria = params
        }

        // if sort is populated, add it to the criteria with the $sort
        if(sort) criteria['$sort'] = sort

        return this
    }

}
