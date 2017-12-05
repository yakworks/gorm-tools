package gorm.tools.hibernate.criteria


import groovy.transform.CompileDynamic
import org.hibernate.criterion.CriteriaSpecification

/**
 * For consistently searching across data types.
 */
@SuppressWarnings(["NestedBlockDepth", "ExplicitCallToAndMethod", "ExplicitCallToOrMethod"])
class CriteriaUtils {

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
}

