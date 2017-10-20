package gorm.tools.hibernate.criteria

import gorm.tools.GormMetaUtils
import gorm.tools.beans.BeanPathTools
import grails.core.GrailsDomainClass
import grails.core.GrailsDomainClassProperty
import groovy.transform.CompileStatic
import org.hibernate.criterion.CriteriaSpecification
import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileDynamic
import org.grails.datastore.mapping.query.api.Criteria

import javax.servlet.http.HttpServletRequest

/**
 * For consistently searching across data types.
 */
@SuppressWarnings(["NestedBlockDepth", "ExplicitCallToAndMethod", "ExplicitCallToOrMethod"])
class CriteriaUtils {

    /** This is a convenience method to redirect all the types of filters from a single map. */
    @CompileDynamic
    static Criteria filterGroup(Map groups) {
        ['filterBoolean', 'filterDate', 'filterDomain', 'filterMoney', 'filterLong', 'filterText', 'filterSimple'].each { command ->
            if (groups[command]) "${command}"(groups.map, groups.delegate, groups[command])
        }
    }

    static Criteria filterBoolean(Map params, delegate, List keys) {
        filterEqIn(params, delegate, { it as Boolean }, keys)
    }

    /** Filters using 'eq' or 'in' as appropriate, a value which does not need to be cast from the json type to the
     * domain type.
     * @param params The search criteria
     * @param delegate The delegate from the createCriteria().list() structure or similar.
     * @param keys The list of attribute names to search on.  Matches the attribute name of the domain object.
     */
    static Criteria filterSimple(Map params, delegate, List keys) { filterEqIn(params, delegate, keys) }

    /** Filters using 'eq' or 'in' as appropriate, a value which needs to be cast from the json type to Long.
     * @param params The search criteria
     * @param delegate The delegate from the createCriteria().list() structure or similar.
     * @param keys The list of attribute names to search on.  Matches the attribute name of the domain object.
     */
    static Criteria filterLong(Map params, delegate, List keys) { filterEqIn(params, delegate, { it as Long }, keys) }

    /** Inserts an 'eq' or 'in' clause depending on whether the value is a List.
     * @param params The search criteria
     * @param delegate The delegate from the createCriteria().list() structure or similar.
     * @param cast A Closure that converts the values to a type matching the domain type.  Defaults to no change.
     * @param keys The list of attribute names to search on.  Matches the attribute name of the domain object.
     */
    @CompileDynamic
    static Criteria filterEqIn(Map params, delegate, Closure cast = { it }, List keys) {
        Closure result = {
            keys.each { key ->
                if (params[key]) {
                    if (params[key] instanceof List) {
                        'in'(key, params[key].collect { cast(it) })
                    } else {
                        eq(key, cast(params[key]))
                    }
                }
            }
        }
        result.delegate = delegate
        return result.call()
    }

    /** Adds a filterRange criteria with a Date cast.  See filterRange for details. */
    static Criteria filterDate(Map params, delegate, List keys) {
        return filterRange(params, delegate, { it as Date }, keys)
    }

    /** Adds a filterRange criteria with a BigDecimal cast.  See filterRange for details. */
    static Criteria filterMoney(Map params, delegate, List keys) {
        return filterRange(params, delegate, { it as BigDecimal }, keys)
    }

    /** Does date or money range comparison constraints.  Should be able to handle any combination of inequalities.
     * Performs up to 2 comparisons on the value in an AND relationship.
     * Can handle any inequality and any sort of 'between-ness' but is safe from injection of odd operators.
     * @param params the parameter map.
     * @param delegate The delegate from within the createCriteria().list() structure.
     * @param cast A Closure which can convert the JSON value to the type in the domain object.
     * @param keys the list of domain attribute names
     * params[key] value needs to be a map which has the following values:
     *     minop:  Optional.  Values: 'eq', 'ne', 'gt', 'ge'
     *     minval: Required if minop exists, otherwise ignored.  A value which can be converted to the domain type
     *             using 'cast' closure.
     *     maxop:  Optional.  Values: 'lt', 'le'
     *     maxval: Required if maxop exists, otherwise ignored.  A value which can be converted to the domain type
     *             using 'cast' closure.
     * The params[key] value needs to have either the min* pair or the max* pair, or both, or the method
     * will have no effect on the query.
     */
    @CompileDynamic
    static Criteria filterRange(Map params, delegate, Closure cast, List keys) {
        Closure result = {
            keys.each { key ->  // spin through each key
                if (params[key]) {   // don't do anything if there's no value
                    Object base = params[key]  // get a localalized map for this key
                    if (base.minop) {  // check for 'minimum' stuff.
                        if (!['eq', 'ne', 'gt', 'ge'].contains(base.minop)) {
                            throw new IllegalArgumentException("Invalid value '${base.minop}' for ${key}.minop value")
                        }
                        "${base.minop}"(key, cast(base.minval))
                    }
                    if (base.maxop) {  // check for 'maximum' stuff.
                        if (!['lt', 'le'].contains(base.maxop)) {
                            throw new IllegalArgumentException("Invalid value '${base.maxop}' for ${key}.maxop value")
                        }
                        "${base.maxop}"(key, cast(base.maxval))
                    }
                }
            }
        }
        result.delegate = delegate
        return result.call()
    }

    /** Constrains the query to one of a list, where the key is a joined attribute in the domain.
     * MUST BE CALLED FROM WITHIN A createCriteria().list() OR SIMILAR CONSTRUCT!
     * @param params is a Map containing parameter data.
     * @param delegate The delegate from within the createCriteria().list() structure.
     * @param keys the list of domain attribute names
     */
    @CompileDynamic
    static Criteria filterDomain(Map params, delegate, List keys) {
        Closure result = {
            keys.each { key ->
                if (params[key]) {
                    "${key}" {
                        if (params[key]?.size() && params[key][0] instanceof Map) {
                            'in'('id', params[key].collect { it.id as Long })
                        } else {
                            'in'('id', params[key].collect { it })
                        }
                    }
                }
            }
        }
        result.delegate = delegate
        return result.call()
    }

    /** Chooses the criteria to use based on the presence of a % in the value.
     * MUST BE CALLED FROM WITHIN A createCriteria().list() OR SIMILAR CONSTRUCT!
     * @param params is a Map containing parameter data.
     * @param delegate The delegate from within the createCriteria().list() structure.
     * @param keys the list of domain attribute names
     */
    @CompileDynamic
    static Criteria filterText(Map params, delegate, keys) {
        Closure result = {
            keys.each { key ->
                if (params[key]) {
                    if (params[key] instanceof List) {
                        'in'(key, params[key])
                    } else {
                        if (params[key].contains('%')) {
                            like key, params[key]
                        } else {
                            eq key, params[key]
                        }
                    }
                }
            }
        }
        result.delegate = delegate  // this is the line that makes it all work within the criteria.
        return result.call()
    }

    /**
     * applies sorting for several columns
     * free-jqgrid that is used on frontend side has feature for multi row sorting
     * By default free-jqrid prepared sorting properties with next pattern
     * sort = columnName(id, name, etc) order(asc|desc), next column order of the last column name is in `order` parametr
     * Example: if user first sorted by name and then by id sort params will be look like [sort: 'name asc, id', order: 'asc']
     *
     * @param params is a Map containing parameter data.
     * @param delegate The delegate from within the createCriteria().list() structure.
     * @param closure closure that should be applyed for each column (should contain order)
     */
    @CompileDynamic
    static Object applyOrder(Map params, delegate, List leftJoinList = null) {
        Closure result = {
            String ordering = [params.sort, params.order].join(" ")
            delegate.and {
                ordering.split(",").each { String order ->
                    String[] sort = order.trim().split(" ")

                    if (leftJoinList) sortLeftJoin(leftJoinList, delegate, sort[0])

                    try {
                        delegate.order(sort[0], sort[1])
                    } catch (MissingMethodException e) {
                        //hack into hibernate to force it using LEFT OUTER JOIN,
                        delegate.order(sort[0], sort[1], true)
                    }
                }
            }
        }
        result.delegate = delegate    // this is the line that makes it all work within the criteria.
        return result.call()
    }

    /**
     * In some cases we need to make a sorting for a field that is null for some rows, as a result rows where value is null
     * wont be shown, to avoid leftjoin should be applied
     *
     * @param list of fields that requires left join
     * @param delegate The delegate from within the createCriteria().list() structure.
     */
    @CompileDynamic
    static List sortLeftJoin(List fieldsList, delegate, sortColumn) {
        Closure result = {
            Closure leftJoin = { field ->
                String[] path = field.split("\\.")
                if (path.size() > 1) {
                    delegate.or {
                        delegate.isNull path[0]
                        "${path[0]}"(CriteriaSpecification.LEFT_JOIN) {
                            delegate.ge 'id', 0L
                            if (path[1].contains(".")) leftJoin.call(path.tail().join("."))
                        }
                    }
                }
            }

            fieldsList.each { field ->
                if (sortColumn.contains(field)) leftJoin.call(field)
            }
        }
        result.delegate = delegate  // this is the line that makes it all work within the criteria.
        return result.call()
    }

    //TODO: add comments
    @CompileDynamic
    static Map typedParams(Map map, String domainName) {
        Map result = [:]
        GrailsDomainClass domainClass = GormMetaUtils.findDomainClass(domainName)
        flattenMap(map).each {String k, v ->
            try {
                if (k == "or"){
                    result["or"] = [:]
                    v.each {String  k1, v1->
                        GrailsDomainClassProperty property = domainClass.getPropertyByName(k1)
                        result["or"][k1] = property.type
                    }
                }
                GrailsDomainClassProperty property = domainClass.getPropertyByName(k)
                result[k] = property.type
            } catch (e) {
                println e
            }

        }
        result
    }

    @CompileDynamic
    static Map flattenMap(Map map, prefix = '') {
        map.inject([:]) { object, v ->
            if (v.value instanceof Map) {
                if (v.key == "or"){
                    object += [or: flattenMap(v.value, "")]
                } else {
                    object += flattenMap(v.value, v.key)
                }
            } else {
                if (v.key.matches(".*[^.]Id")){
                    object["${prefix ? "${prefix}." : ""}${v.key.matches(".*[^.]Id")?v.key.replaceAll("Id\$", ".id").toString():v.key }"] = v.value

                }
                object["${prefix ? "${prefix}." : ""}$v.key"] = v.value
            }
            object
        }
    }

    static Closure restriction = { key, val, type ->
        switch (type) {
            case (String):
                if (val instanceof List) {
                    restrictList(delegate, key, val, type)
                } else {
                    if (val.contains('%')) {
                        like key, val
                    } else {
                        eq key, val
                    }
                }
                break
            case (boolean):
            case (Boolean):
                if (val instanceof List) { //just to handle cases if we get ["true"]
                    'in'(key, val.collect { it.toBoolean() })
                } else {
                    eq(key, val.toBoolean()) // we cant use "asType" because 'false'.asType(Boolean) == true
                }
                break
            case (Long):
            case (Date): //TODO: add date parsing
            case (BigDecimal):
                if (val instanceof List) {
                    restrictList(delegate, key, val, type)
                } else {
                    eq(key, val.asType(type))
                }
                break
        }
    }

    @CompileDynamic
    //TODO: rename
    static search2(Map map, domain) {
        String domainName = domain.name
        Map typedParams = typedParams(map, domainName)
        Map flattened = flattenMap(map)
        Criteria criteria = domain.createCriteria()

        criteria.list() {
            //Used to handle nested properties, if key has ".", for example org.address.id
            //will execute closure address{eq "id", 1}
            Closure nested
            nested = { String key, Closure closure ->
                if (key.contains(".")) {
                    List splited = key.split("[.]")
                    "${splited[0]}" {
                        nested(splited.tail().join("."), closure)
                    }
                } else {
                    closure.call(key) // calls closure with last key in path `id` for our example
                }
            }

            Closure result = {
                typedParams.each { key, type ->
                    if (key == "or"){ //TODO: think how to refactor
                        or {
                            type.each { k, t ->
                                nested.call(k,
                                        { lastKey -> // from nested closure
                                            restriction.delegate = delegate
                                            restriction.call(lastKey, flattened["or"][k], t)
                                        }
                                )
                            }
                        }
                    }
                    nested.call(key,
                            { lastKey -> // from nested closure
                                restriction.delegate = delegate
                                restriction.call(lastKey, flattened[key], type)
                            }
                    )

                }
            }

            result.delegate = delegate  // this is the line that makes it all work within the criteria.
            return result.call()

        }
    }

    //TODO: implements as Enum, currently fails with:
    // Caused by NoClassDefFoundError: Could not initialize class gorm.tools.hibernate.criteria.Statement
    private static List statements = [
            [statements: ["in()", "inList()"], restriction:
                    { delegate, Map params ->
                        delegate.in params.keySet()[0], params.values()[0]
                    }
            ],
            [statements: ["not in()"], restriction:
                    { delegate, Map params ->
                        delegate.not {
                            delegate.in params.keySet()[0], params.values()[0]
                        }
                    }
            ],
            [statements: ["between()"], restriction:
                    { delegate, Map params ->
                        delegate.gte params.keySet()[0], params.values()[0][0]
                        delegate.lte params.keySet()[0], params.values()[0][1]
                    }
            ],
            [statements: ["ilike()"], restriction:
                    { delegate, Map params ->
                        delegate.ilike params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["gt()"], restriction:
                    { delegate, Map params ->
                        delegate.gt params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["gte()"], restriction:
                    { delegate, Map params ->
                        delegate.gte params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["lt()"], restriction:
                    { delegate, Map params ->
                        delegate.lt params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["lte()"], restriction:
                    { delegate, Map params ->
                        delegate.lte params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["ne()"], restriction:
                    { delegate, Map params ->
                        delegate.ne params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["isNull()"], restriction:
                    { delegate, Map params ->
                        delegate.isNull params.keySet()[0], params.values()[0][0]
                    }
            ]
    ]
    @CompileDynamic
    static List<String> listAllowedStatements() {
        statements.collect { it.statements}.flatten()
    }
    @CompileDynamic
    static Closure findRestriction(String statement){
        statements.find{it.statements.contains(statement)}.restriction
    }

    @CompileDynamic
    static private restrictList(delegate, key, list, type){
        if (listAllowedStatements().contains(list[0])){
            List listParams = (list[1] instanceof List) ? list.tail()[0] as List: list.tail()
            findRestriction(list[0]).call(delegate, ["$key":  listParams.collect{type ? it.asType(type): it}])
        } else {
            delegate.inList(key, list.collect { type ? it.asType(type): it })
        }
    }
}



