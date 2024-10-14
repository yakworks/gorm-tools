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
import groovy.util.logging.Slf4j

import gorm.tools.beans.Pager
import yakworks.api.HttpStatus
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemException
import yakworks.commons.map.Maps
import yakworks.json.groovy.JsonEngine

import static gorm.tools.mango.MangoOps.CRITERIA
import static gorm.tools.mango.MangoOps.QSEARCH
import static gorm.tools.mango.MangoOps.SORT

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
@Builder(
    builderStrategy=SimpleStrategy, prefix="",
    includes=['strict', 'projections', 'select', 'timeout'],
    useSetters=true
)
@Slf4j
@CompileStatic
class QueryArgs {

    List<String> ignoreKeys = ['controller', 'action', 'format', 'nd', '_search', 'includes', 'includesKey' ]

    /**
     * the alias for the root entity of the query.
     * MangoDetachedCriteria will default to entity name with "_" suffix ("${entityClass.simpleName}_")
     * NOT USED, POC
     */
    //String rootAlias

    /**
     * extra closure that can be passed to MangoCriteria
     * future
     */
    // Closure closure

    /**
     * The query string is useful for a cache key and for logging
     */
    String queryString

    /**
     * when true in build method, will only add params that are under q.
     * When false(default) and no q is present in the params
     * then build will add any param that are not special(like max, sort, page, etc)
     * into q as a criteria param.
     */
    boolean strict = false
    private void setStrict(boolean v) {
        ensureNotBuilt()
        strict = v
    }

    /**
     * when true, then q criteria is required and will fail if its not provided so it cant list without it
     */
    // boolean qRequired = false
    // private void setQRequired(boolean v) {
    //     ensureNotBuilt()
    //     qRequired = v
    // }

    /**
     * Criteria map to pass to the MangoBuilder. when QueryArgs is built from query params, then this is the q=...
     * This is the one to modify if making changes in an override.
     */
    private Map<String, Object> qCriteria = [:] as Map<String, Object>
    Map<String, Object> getqCriteria() {
        return this.qCriteria
    }
    /**
     * The Pager instance for paged list queries
     */
    private Pager pager
    Pager getPager() {
        pager
    }
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
     * holder for select list
     */
    List<String> select

    /**
     * Query timeout in seconds. If value is set, the timeout would be set on hibernate query/criteria instance.
     */
    Integer timeout = 0

    private boolean isBuilt = false

    /**
     * Construct from a pager
     * DOES NOT BUILD
     */
    static QueryArgs withPager(Pager pager){
        def qa = new QueryArgs()
        qa.pager = pager
        return qa
    }

    /**
     * Construct AND Build from a controller style params object where each key has as string value
     * just as if it came from a url
     */
    static QueryArgs of(Map params){
        def qa = new QueryArgs()
        return qa.build(params)
    }

    /**
     * Construct from a mango closure
     * Future concept
     */
    // static QueryArgs of(@DelegatesTo(MangoDetachedCriteria) Closure closure){
    //     def qa = new QueryArgs()
    //     return qa.query(closure)
    // }

    /**
     * Construct with projections, used mostly for testing
     * Does not build, should call .build after if more is needed
     * Future concept
     */
    // static QueryArgs withProjections(Map<String, String> projs){
    //     def qa = new QueryArgs()
    //     return qa.projections(projs)
    // }

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
        if(isBuilt) throw new UnsupportedOperationException("build has already been called and cant be called again")
        //copy it
        Map params = Maps.clone(paramsMap) as Map<String, Object>

        //remove the fields that grails adds for controller and action
        params.removeAll {it.key in ignoreKeys }

        // pull out the max, page and offset and assume the rest is criteria,
        // if pager is already set then we do nothing with the pagerMap
        Map pagerMap = [:]
        ['max', 'offset', 'page'].each{ String k ->
            if(params.containsKey(k)) pagerMap[k] = params.remove(k)
        }
        // if no pager was set then use what we just removed to set one up
        if(!pager) pager = Pager.of(pagerMap)

        //sorts and orderBy
        String orderBy = params.remove('order') ?: 'asc'
        def sortField = params.remove('sort')
        if(sortField) sort = buildSort(sortField, orderBy)

        //projections
        def projField = params.remove('projections')
        if(projField) projections = buildProjections(projField)

        //projections
        var selField = params.remove('select')
        if(selField) select = buildSelectList(selField)

        // check for and remove the q param
        // whatever is in q if its parsed as a map and set to the criteria so it overrides everything
        def qParam = params.q ? params.remove('q') : params.remove(CRITERIA)
        if(qParam && qParam instanceof String) qParam = qParam.trim()

        if(qParam) {
            if (qParam instanceof String) {
                String qString = qParam as String

                //FIXME
                //if q=* just put it as QSEARCH, it will get removed whn building criteria
                //Its used by Rest tests, otherwise because of qRequired, rests tests cant query without passing any criterias
                if(qString.trim() == "*") {
                    qCriteria[QSEARCH] = qString
                } else {
                    //if the q param start with { then assume its json and parse it
                    //the parsed map will be set to the criteria.
                    qCriteria = parseJson(qString, Map)

                    //clone so it can me modified later
                    qCriteria = Maps.clone(qCriteria)
                }
            }
            //as is, mostly for testing and programtic stuff
            else if(qParam instanceof Map) {
                qCriteria = qParam as Map<String, Object>
            }
        }
        //if no q was passed in then use whatever is left in the params as the criteria if strict is false
        else if(!strict){
            //FIXME should we not be making a copy of this?
            qCriteria = params
        }

        //now check if qSearch was passed as a separate param and its doesn't already exists in the criteria
        String qSearchParam = params.remove('qSearch')
        if(qSearchParam && !qCriteria.containsKey(QSEARCH)){
            qCriteria[QSEARCH] = qSearchParam
        }

        // if sort was populated, add it to the criteria with the $sort if its doesn't exist
        // if(sort && !criteria.containsKey(MangoOps.SORT) ) {
        //     criteria[MangoOps.SORT] = sort
        // }

        //set that it was built
        isBuilt = true

        //validate if qRequired
        //if(qRequired) validateQ()

        return this
    }

    /**
     * builds a COPY of qCrieria merged with sort if it exists and removes the $qSearch=* if it exists
     */
    @Deprecated
    Map<String, Object> buildCriteria(){
        return buildCriteriaMap()
    }

    Map<String, Object> buildCriteriaMap(){
        ensureBuilt()
        Map<String, Object> criterium = qCriteria
        // if sort was populated, add it to the criteria with the $sort if its doesn't exist
        if(sort && !qCriteria.containsKey(SORT) ) {
            criterium = qCriteria + ([(SORT): sort] as Map<String, Object>)
        }
        //remove the qSearch=* if its been passed in.
        if(criterium.containsKey(QSEARCH) && criterium[QSEARCH] == "*") criterium.remove(QSEARCH)
        return criterium
    }
    /**
     * Throws IllegalArgumentException if qRequired is true.
     * This forces it to pick up the q params in case it accidentally or inadvertantly dropped off.
     * Can bypass this by passing in q=* or qSearch=*
     * @throws DataProblemException
     */
    QueryArgs validateQ(boolean qRequired){
        ensureBuilt()
        //put in initially because we loose params query parsing / lost params issue is fixed - See #1924
        if(qRequired && !qCriteria){
            throw DataProblem.of('error.query.qRequired')
                .status(HttpStatus.I_AM_A_TEAPOT) //TODO 418 error for now so its easy to add to retry as it gets droppped sometimes
                .title("q or qSearch parameter restriction is required").toException()
        }
        return this
    }

    /**
     * Applies Default sort by id:asc if no sort is provided in params
     * Does not apply default sort if
     * - $sort is provided in `q` criteria
     * - If params has projections - because then there's no id column.
     *   In this case, if required, params should explicitely pass sort
     *
     * when paging we need a sort so rows dont show up next page see https://github.com/9ci/domain9/issues/2280
     */
    QueryArgs defaultSortById() {
        ensureBuilt()
        if(!sort && !projections) {
            sort = ['id':'asc']
        }
        return this
    }

    /**
     * if the string is known to be json then parse the json and returns the map
     */
    static <T> T parseJson(String text, Class<T> clazz) {
        try {
            //jsonSlurper LAX allows fields to not be quoted
            JsonSlurper jsonSlurper = new JsonSlurper().setType(JsonParserType.LAX)
            Object parsedObj = jsonSlurper.parseText(text)
            JsonEngine.validateExpectedClass(clazz, parsedObj)
            return (T)parsedObj
        } catch (ex) {
            //JsonException
            throw DataProblem.of('error.query.invalid')
                .detail("Invalid JSON. Error parsing query - $ex.message")
                .toException()
        }

    }

    /**
     * parses the sort string. if its just a simple string without , or : then creates a
     * asc sort map. if its starts with { then parses as json.
     * sort string should be in one of the following formats
     *  - simple field name such as 'name'
     *  - field seperated by : such as 'name:desc'
     *  - multiple fields seperated by comma, ex: 'num:asc, name:desc'
     *  - json in same format as above, ex '{num:"asc", name:"desc"}' but parses simply by stripping out the { and "
     *
     * @param sortObj see above for valid options
     * @param orderBy only relevant if sortText is a single sort string with field name
     * @return the sort Map or null if failed
     */
    protected Map buildSort(Object sortObj, String orderBy = 'asc'){
        if(sortObj instanceof Map) {
            return sortObj
        } else if(sortObj instanceof String) {
            //make sure its trimmed
            String sortText = sortObj.trim()
            Map sortMap = [:] as Map<String, String>
            //sort just looks like json in case api programmer wants to be consistent.
            //but its a query param and we really expect it in the format like  sort=foo:asc,bar:desc
            // so we convert something passes as json like  q={"foo":"asc","bar":"desc"} by simply stripping out the " and the {
            // We DONT use json pareser since it messes up the order, and the order matters here.
            sortText = sortText.replaceAll(/[}{'"]/, "")
            //will only be one item in list if no ',' token
            List sortList = sortText.tokenize(',')*.trim() as List<String>
            for (String sortEntry : sortList) {
                if (sortText.contains(':')) {
                    List sortTokens = sortEntry.tokenize(':')*.trim() as List<String>
                    sortMap[sortTokens[0]] = sortTokens[1]
                } else {
                    //its should just a field name
                    sortMap[sortEntry] = orderBy
                }
            }

            return sortMap
        } else {
            log.error("sort argument must be map or string")
            return [:]
        }
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
    protected Map buildProjections(Object projectionsObj){
        if(projectionsObj instanceof Map) {
            return projectionsObj
        } else if(projectionsObj instanceof String){
            //make sure its trimmed
            String projText = (projectionsObj as String).trim()
            //for convienience we allow the { to be left off so we add it if it is
            if (!projText.startsWith('{')) projText = "{$projText}"
            // clone since parseText returns LazyValueMap which will throw `Not that kind of map` when trying to add new key
            Map projMap = Maps.clone(parseJson(projText, Map))
            return projMap
        } else {
            log.error("projection argument must be map or string")
            return [:]
        }

    }

    /**
     * If its a list then just returns it.
     * Otherwise parses the select string. If it start with [ and is a string it will parse as json.
     *
     * parse string should be in one of the following formats
     *  - fields seperated by comma, ex: 'id,name,num,foo.bar'
     *  - json in same format as above, ex '["id","name"]'
     *
     * @param qSelect see above for valid options
     * @return the list or null if failed
     */
    protected List<String> buildSelectList(Object qSelect){
        if(qSelect instanceof List) {
            return qSelect
        } else if(qSelect instanceof String){
            //make sure its trimmed
            String selectText = (qSelect as String).trim()
            //for convenience we allow the [ to be left off so we add it if it is
            if (!selectText.startsWith('[')) selectText = "[$selectText]"

            List parsedList = (List<String>)parseJson(selectText, List)
            return parsedList
        } else {
            log.error("projection argument must be map or string")
            return [] as List<String>
        }

    }

    // throws error if its not built yet
    private void ensureBuilt(){
        if(!isBuilt) throw new UnsupportedOperationException("Can only be called after this has been built")
    }
    // throws error if its already built
    private void ensureNotBuilt(){
        if(isBuilt) throw new UnsupportedOperationException("Can't be called after its already built")
    }

    // QueryArgs query(@DelegatesTo(MangoDetachedCriteria) Closure closure) {
    //     this.closure = closure
    //     return this
    // }

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
