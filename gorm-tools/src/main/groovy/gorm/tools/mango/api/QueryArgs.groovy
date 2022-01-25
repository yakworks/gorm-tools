/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api

import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import gorm.tools.beans.Pager
import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.MangoOps
import yakworks.commons.map.Maps

import static gorm.tools.mango.MangoOps.CRITERIA

/**
 * Builder arguments for a query to pass from controllers etc to the MangoQuery
 * Can think of it a bit like a SQL query.
 *
 * This holds
 *  - what we want to select (includes)
 *  - projections if it s projection query (projections)
 *  - the where conditions (criteria)
 *  - the orderBy (sort)
 *  - and how we want the paging to be handled (pager), number of items per page, etc..
 *
 * contains some intermediary fields such as 'q' that are used to parse it into what we need
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class QueryArgs {

    /**
     * extra closure that can be passed to MangoCriteria
     */
    Closure closure

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
     * holder for projections
     * The key is the field, can be dot path for nested like foo.bar.baz
     * The value is one of 'group', 'sum', 'count'
     */
    Map<String, String> projections

    /**
     * Construct from a pager
     */
    static QueryArgs of(Pager pager){
        def qa = new QueryArgs()
        qa.pager = pager
        return qa
    }

    /**
     * Construct from a controller style params object where each key has as string value
     * just as if it came from a url
     */
    static QueryArgs of(Map params){
        def qa = new QueryArgs()
        return qa.build(params)
    }

    /**
     * Construct from a mango closure
     */
    static QueryArgs of(@DelegatesTo(MangoDetachedCriteria) Closure closure){
        def qa = new QueryArgs()
        return qa.query(closure)
    }

    static QueryArgs withProjections(Map<String, String> projs){
        def qa = new QueryArgs()
        return qa.projections(projs)
    }

    /**
     * Construct with a criteria map as is.
     */
    static QueryArgs withCriteria(Map<String, Object> crit){
        def qa = new QueryArgs()
        return qa.criteria(crit)
    }

    /**
     * Intelligent defaults to setup the criteria and pager from the controller style params map
     *
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
        Map params = Maps.clone(paramsMap) as Map<String, Object>

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
            sort = buildSort(sortField, orderBy)
        }

        //projections
        String projField = (params.remove('projections') as String)?.trim()
        if(projField) projections = buildProjections(projField)

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
                    criteria = parseJson(qString)
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
    Map parseJson(String qString){
        //jsonSlurper LAX allows fields to not be quoted
        JsonSlurper jsonSlurper = new JsonSlurper().setType(JsonParserType.LAX)
        // parseText returns LazyValueMap which will throw `Not that kind of map` when trying to add new key
        Map parsedMap = new HashMap(jsonSlurper.parseText(qString) as Map)

        return parsedMap
    }

    /**
     * parses the sort string. if its just a simple string without , or : then creates a
     * asc sort map. if its starts with { then parses as json.
     * sort string should be in one of the following formats
     *  - simple field name such as 'name'
     *  - field seperated by : such as 'name:desc'
     *  - multiple fields seperated by comma, ex: 'num:asc, name:desc'
     *  - json in same format as above, ex '{num:"asc", name:"desc"}'
     *
     * @param sortText see above for valid options
     * @param orderBy only relevant if sortText is a single sort string with field name
     * @return the sort Map or null if failed
     */
    Map buildSort(String sortText, String orderBy = 'asc'){
        //make sure its trimmed
        sortText = sortText.trim()
        Map sortMap = [:] as Map<String, String>
        //if its starts with { its json and we take it as it is
        if (sortText.startsWith('{')) {
            sortMap = parseJson(sortText) as Map<String, String>
        }
        else if(sortText.contains(':')) {
            //will only be one item in list if no ',' token
            List sortList = sortText.tokenize(',')*.trim() as List<String>
            for(String sortEntry : sortList){
                List sortTokens = sortEntry.tokenize(':')*.trim() as List<String>
                sortMap[sortTokens[0]] = sortTokens[1]
            }
        } else {
            //its just a field name
            sortMap[sortText] = orderBy
        }

        return sortMap
    }

    /**
     * parses the projection string. If it start with { and will parse as json.
     * parse string should be in one of the following formats
     *  - fields seperated by comma, ex: 'type:group,calc.totalDue:sum'
     *  - json in same format as above, ex '{type:"group", "calc.totalDue":"sum"}'
     *
     * @param projText see above for valid options
     * @return the projection Map or null if failed
     */
    Map buildProjections(String projText){
        //make sure its trimmed
        projText = projText.trim()
        Map projMap = [:] as Map<String, String>
        //for convienience we allow the { to be left off so we add it if it is
        if (!projText.startsWith('{')) projText = "{$projText}"

        projMap = parseJson(projText) as Map<String, String>

        return projMap
    }

    QueryArgs query(@DelegatesTo(MangoDetachedCriteria) Closure closure) {
        this.closure = closure
        return this
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
