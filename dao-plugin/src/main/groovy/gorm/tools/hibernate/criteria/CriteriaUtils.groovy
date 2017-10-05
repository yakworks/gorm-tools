package gorm.tools.hibernate.criteria

import org.hibernate.criterion.CriteriaSpecification

/**
 * For consistently searching across data types.
 */
public class CriteriaUtils {

    /** This is a convenience method to redirect all the types of filters from a single map. */
    static def filterGroup(Map groups) {
        ['filterBoolean', 'filterDate', 'filterDomain', 'filterMoney', 'filterLong', 'filterText', 'filterSimple'].each { command ->
            if (groups[command]) "${command}"(groups.map, groups.delegate, groups[command])
        }
    }

    static def filterBoolean(Map params, delegate, List keys) {
        filterEqIn(params, delegate, { it as Boolean }, keys)
    }

    /** Filters using 'eq' or 'in' as appropriate, a value which does not need to be cast from the json type to the
     * domain type.
     * @param params The search criteria
     * @param delegate The delegate from the createCriteria().list() structure or similar.
     * @param keys The list of attribute names to search on.  Matches the attribute name of the domain object.
     */
    static def filterSimple(Map params, delegate, List keys) { filterEqIn(params, delegate, keys) }

    /** Filters using 'eq' or 'in' as appropriate, a value which needs to be cast from the json type to Long.
     * @param params The search criteria
     * @param delegate The delegate from the createCriteria().list() structure or similar.
     * @param keys The list of attribute names to search on.  Matches the attribute name of the domain object.
     */
    static def filterLong(Map params, delegate, List keys) { filterEqIn(params, delegate, { it as Long }, keys) }

    /** Inserts an 'eq' or 'in' clause depending on whether the value is a List.
     * @param params The search criteria
     * @param delegate The delegate from the createCriteria().list() structure or similar.
     * @param cast A Closure that converts the values to a type matching the domain type.  Defaults to no change.
     * @param keys The list of attribute names to search on.  Matches the attribute name of the domain object.
     */
    static def filterEqIn(Map params, delegate, Closure cast = { it }, List keys) {
        def result = {
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
        return result()
    }

    /** Adds a filterRange criteria with a Date cast.  See filterRange for details. */
    static def filterDate(Map params, delegate, List keys) {
        return filterRange(params, delegate, { it as Date }, keys)
    }

    /** Adds a filterRange criteria with a BigDecimal cast.  See filterRange for details. */
    static def filterMoney(Map params, delegate, List keys) {
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
    static def filterRange(Map params, delegate, Closure cast, List keys) {
        def result = {
            keys.each { key ->  // spin through each key
                if (params[key]) {   // don't do anything if there's no value
                    def base = params[key]  // get a localalized map for this key
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
        return result()
    }

    /** Constrains the query to one of a list, where the key is a joined attribute in the domain.
     * MUST BE CALLED FROM WITHIN A createCriteria().list() OR SIMILAR CONSTRUCT!
     * @param params is a Map containing parameter data.
     * @param delegate The delegate from within the createCriteria().list() structure.
     * @param keys the list of domain attribute names
     */
    static def filterDomain(Map params, delegate, List keys) {
        def result = {
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
        return result()
    }

    /** Chooses the criteria to use based on the presence of a % in the value.
     * MUST BE CALLED FROM WITHIN A createCriteria().list() OR SIMILAR CONSTRUCT!
     * @param params is a Map containing parameter data.
     * @param delegate The delegate from within the createCriteria().list() structure.
     * @param keys the list of domain attribute names
     */
    static def filterText(params, delegate, keys) {
        def result = {
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
        return result()
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
    static def applyOrder(params, delegate, leftJoinList = null) {
        def result = {
            def ordering = [params.sort, params.order].join(" ")
            delegate.and {
                ordering.split(",").each {
                    def sort = it.trim().split(" ")

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
        return result()
    }

    /**
     * In some cases we need to make a sorting for a field that is null for some rows, as a result rows where value is null
     * wont be shown, to avoid leftjoin should be applied
     *
     * @param list of fields that requires left join
     * @param delegate The delegate from within the createCriteria().list() structure.
     */
    static def sortLeftJoin(fieldsList, delegate, sortColumn) {
        def result = {
            def leftJoin
            leftJoin = { field ->
                def path = field.split("\\.")
                if (path.size() > 1) {
                    delegate.or {
                        delegate.isNull path[0]
                        "${path[0]}"(CriteriaSpecification.LEFT_JOIN) {
                            delegate.ge 'id', 0L
                            if (path[1].contains(".")) leftJoin(path.tail().join("."))
                        }
                    }
                }
            }

            fieldsList.each { field ->
                if (sortColumn.contains(field)) leftJoin(field)
            }
        }
        result.delegate = delegate  // this is the line that makes it all work within the criteria.
        return result()
    }
}
