/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j

import yakworks.commons.model.TotalCount
import yakworks.meta.MetaMapList

/**
 * a holder object for both pager settings and the paged data, used mostly in the rest and views
 * but can also be used for paging through and processing data
 */
@Slf4j
@CompileStatic
@SuppressWarnings('ConfusingMethodName') //for max and page
//XXX VOODOO Security and logging sometimes fails when this has and EqualsAndHashCode
@EqualsAndHashCode //(includes=["page", "max", "data", "params"])
class Pager implements Serializable{

    /**
     * The page we are on, pageNumber
     */
    Integer page = 1

    /**
     * Max rows to show, the pageSize
     */
    Integer max = 20

    /**
     * the max rows the user can set it to
     */
    Integer allowedMax = 10000

    /**
     * The total record count. This is used to calculate the number of pages
     */
    Integer recordCount = 0

    /**
     * Offset max * (page - 1)
     */
    Integer offset

    /**
     * List of elements
     */
    List<Map> data

    /**
     * Parameters
     */
    Map params

    /**
     * Constructor without params
     */
    Pager() {}

    /**
     * Constructor with params
     */
    Pager(Map params) {
        setParams(params)
    }

    static Pager of(Map params){
        def pg = new Pager()
        pg.setParams(params)
        return pg
    }

    /**
     * Computes max value, if max not specified, then 20
     * Default max allowed value is 100
     *
     * @param p map of params that may contain max value
     * @param defaultMax default max value if max in params isnt set
     * @return max value for pagination
     */
    // static Integer max(Map p, Integer defaultMax = 100) {
    //     Integer defmin = p.max ? toInteger(p.max) : 20
    //     p.max = Math.min(defmin, defaultMax)
    //     return p.max as Integer
    // }

    /**
     * Computes page number, if not passed then 1
     *
     * @param p map of params
     * @return page number
     */
    static Integer page(Map p) {
        p.page = p.page ? toInteger(p.page) : 1
        return p.page as Integer
    }

    /**
     * Set params for Pager
     *
     * @param params map of params
     */
    void setParams(Map params) {
        page = params.page = params.page ? toInteger(params.page) : 1
        max = params.max = Math.min(params.max ? toInteger(params.max) : 20, allowedMax)
        this.params = params
        if(params.data) this.data = params.data as List
    }

    /**
     *  Converts object to integer
     *
     * @param v value
     * @return value as integer
     */
    @CompileDynamic
    static Integer toInteger(Object v) {
        return v.toInteger()
    }

    /**
     * Calculates offset based on page number and max value
     *
     * @return offset value
     */
    Integer getOffset() {
        if (!offset) {
            return (max * (page - 1))
        }
        return offset
    }

    /**
     * Calculates total number of pages
     *
     * @return total number of pages
     */
    Integer getPageCount() {
        return Math.ceil((Double) (recordCount / max)).intValue()
    }

    /**
     * Calls passed closure for each page
     *
     * @param c closure that should be called for pages
     */
    void eachPage(Closure c) {
        if (pageCount < 1) return
        log.debug "eachPage total pages : pageCount"

        (1..pageCount).each { Integer pageNum ->
            page = pageNum
            offset = (max * (page - 1))
            try {
                log.debug "Executing eachPage closer with [max:$max, offset:$offset]"
                c.call(max, offset)
            } catch (e) {
                log.error "Error encountered while calling closure in eachPage [max:$max, offset:$offset]}]", e
                throw e
            }
        }

    }

    /**
     * Sets the data from a MetaMapList.
     * Does nothing special to data but sets the RecordCount from getTotalCount()
     *
     * @param metaMapList the list to use for the data
     * @return reference to this Pager
     */
    Pager dataList(List<Map> metaMapList) {
        if(metaMapList){
            if(metaMapList instanceof TotalCount) {
                setRecordCount(((TotalCount) metaMapList).getTotalCount())
            } else {
                setRecordCount(metaMapList.size())
            }

            setData(metaMapList as List<Map>)
        } else {
            setRecordCount(0)
            setData([])
        }
        return this
    }

    @Deprecated
    Pager setMetaMapList(MetaMapList metaMapList) {
        return dataList(metaMapList as List<Map>)
    }


}
