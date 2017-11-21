package gorm.tools.hibernate.criteria

import gorm.tools.GormMetaUtils
import gorm.tools.Pager
import gorm.tools.beans.DateUtil
import grails.converters.JSON
import grails.core.GrailsDomainClass
import grails.core.GrailsDomainClassProperty
import groovy.transform.CompileDynamic
import org.grails.datastore.mapping.query.api.Criteria
import org.hibernate.criterion.CriteriaSpecification

/**
 * For consistently searching across data types.
 */
@SuppressWarnings(["NestedBlockDepth", "ExplicitCallToAndMethod", "ExplicitCallToOrMethod"])
class CriteriaUtils {

    /** This is a convenience method to redirect all the types of filters from a single map. */
    @Deprecated
    @CompileDynamic
    static Criteria filterGroup(Map groups) {
        ['filterBoolean', 'filterDate', 'filterDomain', 'filterMoney', 'filterLong', 'filterText', 'filterSimple'].each { command ->
            if (groups[command]) "${command}"(groups.map, groups.delegate, groups[command])
        }
    }

    @Deprecated
    static Criteria filterBoolean(Map params, delegate, List keys) {
        filterEqIn(params, delegate, { it as Boolean }, keys)
    }

    /** Filters using 'eq' or 'in' as appropriate, a value which does not need to be cast from the json type to the
     * domain type.
     * @param params The search criteria
     * @param delegate The delegate from the createCriteria().list() structure or similar.
     * @param keys The list of attribute names to search on.  Matches the attribute name of the domain object.
     */
    @Deprecated
    static Criteria filterSimple(Map params, delegate, List keys) { filterEqIn(params, delegate, keys) }

    /** Filters using 'eq' or 'in' as appropriate, a value which needs to be cast from the json type to Long.
     * @param params The search criteria
     * @param delegate The delegate from the createCriteria().list() structure or similar.
     * @param keys The list of attribute names to search on.  Matches the attribute name of the domain object.
     */
    @Deprecated
    static Criteria filterLong(Map params, delegate, List keys) { filterEqIn(params, delegate, { it as Long }, keys) }

    /** Inserts an 'eq' or 'in' clause depending on whether the value is a List.
     * @param params The search criteria
     * @param delegate The delegate from the createCriteria().list() structure or similar.
     * @param cast A Closure that converts the values to a type matching the domain type.  Defaults to no change.
     * @param keys The list of attribute names to search on.  Matches the attribute name of the domain object.
     */
    @Deprecated
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
    @Deprecated
    static Criteria filterDate(Map params, delegate, List keys) {
        return filterRange(params, delegate, { it as Date }, keys)
    }

    /** Adds a filterRange criteria with a BigDecimal cast.  See filterRange for details. */
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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

    /**
     * To be able to make search we need to know types for each filter parameter,
     * the method flattens filters, and instead of values returns type
     *
     * @param map filters for search
     * @param domainName name of the domain class
     * @return map where key - is path to variable and value - its type
     */
    @CompileDynamic
    static Map typedParams(Map map, String domainName) {
        Map result = [:]
        GrailsDomainClass domainClass = GormMetaUtils.findDomainClass(domainName)
        flattenMap(map).each { String k, v ->
            try {
                String checkedKey = k
                List <String> splited = k.split("[.]")
                if (splited.find{it.matches(/\$(or|and)(,\d*)?$/)}) {
                    checkedKey = splited.findAll { !it.matches(/\$(or|and)(,\d*)?$/) }.join(".")
                }
                GrailsDomainClassProperty property = domainClass.getPropertyByName(checkedKey)
                result[k] = toType(v, property.type)
            } catch (e) {
                println e
            }

        }
        result
    }

    /**
     * Recorsive method that flattens map [customer:[id: 1]] -> [customer.id: 1]
     * Specific for criteriaUtils to handle [customerId: 1]  -> [customer.id: 1]
     *
     * @param params map of params
     * @return flattened map
     */
    @SuppressWarnings(["AbcMetric"])
    @CompileDynamic
    static Map flattenMap(Map params) {
        Closure flatMap
        flatMap = { map, prefix = '' ->
            int i= 0
            map.inject([:]) { object, v ->
                if (gorm.tools.hibernate.criteria.Statements.listAllowedStatements(gorm.tools.hibernate.criteria.StatementsType.OPERATORS).contains(v.key) && v.value instanceof List) {
                    int j = 0
                    v.value.each { Map listV ->
                        object += flatMap(listV, "${prefix ? "${prefix}." : ""}${v.key},${i}.\$and,${j++}")
                    }
                } else {
                    if (v.value instanceof Map) {
                        object += flatMap(v.value, "${prefix ? "${prefix}." : ""}$v.key")
                    } else {
                        // mostly for handling 'isNull' it could be as value {credit: "$isNull"} and key {credit: {$isNull: true}}
                        if (gorm.tools.hibernate.criteria.Statements.listAllowedStatements(gorm.tools.hibernate.criteria.StatementsType.UNARY).intersect([v.value.toString()])) {
                            object["${prefix ? "${prefix}." : ""}$v.key"] = [v.value.toString()]
                        } else if (gorm.tools.hibernate.criteria.Statements.listAllowedStatements(gorm.tools.hibernate.criteria.StatementsType.UNARY).intersect([v.key])) {
                            object["${prefix ? "${prefix}." : ""}$v.key"] = [v.key]
                        } else {
                            if (v.key.matches(".*[^.]Id")) {
                                object["${prefix ? "${prefix}." : ""}${v.key.matches(".*[^.]Id") ? v.key.replaceAll("Id\$", ".id").toString() : v.key}"] = v.value
                            }
                            object["${prefix ? "${prefix}." : ""}$v.key"] = v.value
                        }
                    }
                }
                i++
                object
            }
        }
        Map res = [:]
        flatMap(params).each { k, v ->
            if (k.split("[.]")[-1] && gorm.tools.hibernate.criteria.Statements.listAllowedStatements().contains(k.split("[.]")[-1])) {
                res[k.split("[.]")[0..-2].join(".")] = [k.split("[.]")[-1], v]
            } else {
                res[k] = v
            }
        }
        res
    }

    /**
     * Closure that runs criteria restrictions for certain types
     */
    static Closure restriction = { key, val, type ->
        if (val != "")
            switch (type) {
                case (String):
                    if (val instanceof List) {
                        restrictList(delegate, key, val)
                    } else {
                        if (val && val.contains('%')) {
                            like key, val
                        } else {
                            eq key, val
                        }
                    }
                    break
                case [Boolean, boolean]:
                    if (val instanceof List) { //just to handle cases if we get ["true"]
                        'in'(key, val*.toBoolean())
                    } else {
                        eq(key, val.toBoolean()) // we cant use "asType" because 'false'.asType(Boolean) == true
                    }
                    break
                case (Date):
                    if (val instanceof List) {
                        restrictList(delegate, key, val)
                    } else {
                        eq(key, toDate(val))
                    }
                    break
                case [Integer, int, long, double, Double, float, Float, Long, BigDecimal]:
                    if (val instanceof List || (val instanceof String && val.matches(/\[.*\]/))) {
                        restrictList(delegate, key, val)
                    } else {
                        eq(key, val)
                    }
                    break
            }
    }

    static Date toDate(val) {
        if (val instanceof String) {
            DateUtil.parseJsonDate(val)
        } else {
            val instanceof Date ? val : new Date(val) //for case when miliseconds are passed
        }
    }

    @CompileDynamic
    static toType(val, type) {

        if (val instanceof List) {
            //For property statements we shoudnt transform property name to specific type
            if (gorm.tools.hibernate.criteria.Statements.listAllowedStatements(gorm.tools.hibernate.criteria.StatementsType.PROPERTY).contains(val[0])) {
                return val
            }
            return val.collect {
                gorm.tools.hibernate.criteria.Statements.listAllowedStatements().contains(it) ? it : toType(it, type)
            }
        }

        switch (type) {
            case String:
                val
                break
            case [Boolean, boolean]:
                val.toBoolean()// we cant use "asType" because 'false'.asType(Boolean) == true
                break
            case (Date):
                toDate(val)
                break
            case [Integer, int, long, double, Double, float, Float, Long, BigDecimal]:
                val.asType(type)
                break
        }
    }

    @CompileDynamic
    static getType(val) {
        if (val instanceof List) return val[0].getClass()
        val.getClass()
    }

    /**
     * Core list method that runs search
     *
     * @param filters map of properties to search on
     * @param domain domain class that should be used for search
     * @return list of entities
     */
    @SuppressWarnings(['GStringAsMapKey'])
    @CompileDynamic
    static List list(Map filters, Class domain, Map params = [:], Closure closure = null) {
        Criteria criteria = domain.createCriteria()
        Pager pager = new Pager(params)
        criteria.list(max: pager.max, offset: pager.offset) {
            if (closure) {
                closure.delegate = delegate
                closure.call()
            }
            if ((filters.quickSearch || filters.q) && domain.quickSearchFields) {
                String quickParams = filters.quickSearch ?: filters.q
                criterias.delegate = delegate
                criterias.call(["\$or": domain.quickSearchFields.collectEntries {
                    ["$it": ["\$quickSearch", quickParams]]
                }], domain, params)
                return
            }
            criterias.delegate = delegate
            criterias.call(filters, domain, params)
        }
    }

    @CompileDynamic
    static List list(String filters, Class domain, Map params = [:], Closure closure = null) {
        list(JSON.parse(filters) as Map, domain, params, closure)
    }

    /**
     * Core get that runs search
     *
     * @param filters map of properties to search on
     * @param domain domain class that should be used for search
     * @return result
     */
    @SuppressWarnings(['GStringAsMapKey'])
    @CompileDynamic
    static List countTotals(Map filters, Class domain, Map params = [:], Closure closure) {
        Criteria criteria = domain.createCriteria()
        criteria.get {
            criterias.delegate = delegate
            criterias.call(filters, domain, params)
            if ((filters.quickSearch || filters.q) && domain.quickSearchFields) {
                String quickParams = filters.quickSearch ?: filters.q
                criterias.delegate = delegate
                String searchValue = quickParams.contains("%") ? quickParams : quickParams + "%"
                criterias.call([or: domain.quickSearchFields.collectEntries {
                    ["$it": ["quickSearch()", searchValue]]
                }], domain, params)
                return
            }
            closure.delegate = delegate
            closure.call()
        }
    }

    static Closure criterias = { Map filters, Class domain, Map params = [:] ->
        String domainName = domain.name
        Map typedParams = typedParams(filters, domainName)
        Closure result = {

            Closure run
            run = { map, Closure closure ->
                map.keySet().each { k ->
                    if (map[k] instanceof Map) {
                        "${k.matches(/\$(or|and)(,\d*)?$/) ? k[1..-1].split(",")[0] : k}" {
                            run(map[k], closure)
                        }
                    } else {
                        closure.call(k, map[k])
                    }

                }
            }
            run.call(toNestedMap(typedParams)) { lastKey, val -> // from nested closure
                restriction.delegate = delegate
                restriction.call(lastKey, val, getType(val))
            }

            if (params.order) {
                if (params.sort && params.order) {
                    applyOrder(params, delegate)
                } else {
                    (params.order instanceof String ? Eval.me(params.order) : params.order).each { k, v ->
                        order(k, v)
                    }
                }

            }
        }

        result.delegate = delegate  // this is the line that makes it all work within the criteria.
        return result.call()
    }

    /**
     * Applies statements to criteria form list.
     *
     * @param delegate criteria delegate
     * @param key path to variable
     * @param list list of filters, first element could be restriction method name
     * @param type variable type
     */
    @CompileDynamic
    static private restrictList(delegate, key, list) {
        if (list instanceof String) list = Eval.me(list)
        if (Statements.listAllowedStatements().contains(list[0])) {
            List listParams = (list[1] instanceof List) ? list.tail()[0] as List : list.tail()
            if ((listParams[0] && listParams[0] != "") | gorm.tools.hibernate.criteria.Statements.listAllowedStatements(gorm.tools.hibernate.criteria.StatementsType.UNARY).contains(list[0])) {
                Statements.findRestriction(list[0]).call(delegate, ["$key": listParams])
            }
        } else {
            if (!list.isEmpty()) {
                delegate.inList(key, list)
            }
        }
    }

    static private Map toNestedMap(Map flattenedMap) {
        Closure putNestedValue
        putNestedValue = { Map res, String key, val ->
            if (key.contains(".")) {
                String newKey = key.split("[.]")[0]
                if (!res[newKey]) res[newKey] = [:]
                putNestedValue(res[newKey], key.split("[.]").tail().join("."), val)
            } else {
                if (!res[key]) res[key] = [:]
                res[key] = val
            }
        }

        Map res = [:]
        flattenedMap.each { k, v ->
            putNestedValue(res, k, v)
        }
        res
    }
}

