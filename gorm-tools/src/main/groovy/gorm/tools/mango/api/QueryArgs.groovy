/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api

import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import gorm.tools.beans.Pager
import gorm.tools.mango.MangoOps
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
     * holder for sort configuration to make it easier to grok
     * The key is the field, can be dot path for nested like foo.bar.baz
     * The value is either 'asc' or 'desc'
     */
    Map<String, String> sort

    /**
     * Intelligent defaults to setup the criteria and pager from the paramsMap
     *  - looks for q param and parse if json object (starts with {)
     *  - or sets up the $qSearch map if its text
     *  - if qSearch is provided as separate param along with q then adds it as a $qSearch
     *  - for any of above options for $qSearch sets up object with configured qSearchIncludes
     *
     * Pager
     *  - if pager key is passed in then uses that
     *  - removes 'max', 'offset', 'page' and sets up pager object if not passed in
     *
     * Sort and Order
     *  - sets up an '$sort' map if sort or order key are passed in
     *
     * Translates q from json
     * example params= [q: "{foo: 'test*'}", sort:'foo', page: 2, offset: 10]
     * criteria= [foo:'test*', $sort:'foo'] and pager will be setup
     *
     * Transalates qSearch fields when q is a string
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

        // check for and remove the q param
        // whatever is in q if its parsed as a map and set to the criteria so it overrides everything
        def qParam = params.q ? params.remove('q') : params.remove(CRITERIA)
        if(qParam && qParam instanceof String) qParam = qParam.trim()

        if(qParam) {
            if (qParam instanceof String) {
                String qString = qParam as String
                Map parsedMap
                //if the q param start with { then assume its json and parse it
                //the parsed map will be set to the criteria.
                if (qString.trim().startsWith('{')) {
                    criteria = buildWithJson(qString)
                } else {
                    criteria[MangoOps.QSEARCH] = qString
                }
            }
            //as is, mostly for testing and programtic stuff
            else if(qParam instanceof Map) {
                criteria = qParam as Map
            }
        }
        //if no q was passed in then use whatever is left in the params as the criteria
        else {
            criteria = params
        }

        //now check if qSearch was passed as a separate param and its doesn't already exists in the criteria
        String qSearchParam = params.remove('qSearch')
        if(qSearchParam && !criteria.containsKey(MangoOps.QSEARCH)){
            criteria[MangoOps.QSEARCH] = qSearchParam
        }

        // if sort was populated, add it to the criteria with the $sort if its doesn't exist
        if(sort && !criteria.containsKey('$sort') ) {
            criteria['$sort'] = sort
        }

        return this
    }

    /**
     * if the string is known to be json then parse the json and returns the map
     * also adds in the includes if its has a $qSearch prop
     */
    Map buildWithJson(String qString){
        //jsonSlurper LAX allows fields to not be quoted
        JsonSlurper jsonSlurper = new JsonSlurper().setType(JsonParserType.LAX)
        // parseText returns LazyValueMap which will throw `Not that kind of map` when trying to add new key
        Map parsedMap = new HashMap(jsonSlurper.parseText(qString) as Map)

        return parsedMap
    }

    /**
     * looks for the qsearch fields for this entity and returns the map
     * like [text: "foo", 'fields': ['name', 'num']]
     * if no qSearchFields then its just returns [text: "foo"]
     */
    // Map<String, Object> makeQSearchMap(String searchText){
    //     Map qMap = [text: searchText] as Map<String, Object>
    //     if (qSearchFields) {
    //         qMap['fields'] = qSearchFields
    //     }
    //     return qMap
    // }

}