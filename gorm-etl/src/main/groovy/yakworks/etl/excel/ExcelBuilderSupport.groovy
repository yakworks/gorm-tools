/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.etl.excel

import groovy.transform.CompileStatic

import yakworks.commons.beans.PropertyTools
import yakworks.commons.lang.LabelUtils
import yakworks.commons.lang.Validate
import yakworks.commons.map.MapFlattener
import yakworks.gorm.api.ApiConfig
import yakworks.meta.MetaMapList
import yakworks.meta.MetaProp

/**
 * Extra support for excel such us using whats in the config to setup the props and headers
 */
@CompileStatic
class ExcelBuilderSupport {

    /**
     * kind of a HACK until we clean up the new config. defaults to using whats in gridOptions.colModel
     */
    static ExcelBuilder useIncludesConfig(ExcelBuilder eb, ApiConfig apiConfig, List<Map> dataList, String entityClass){

        if(dataList instanceof MetaMapList && dataList.metaEntity){
            //flatten to get the titles
            entityClass = dataList.metaEntity.className
            Map<String, MetaProp> metaProps = dataList.metaEntity.flatten()
            eb.includes = metaProps.keySet().toList()
            // eb.headerTitles = metaProps.values().collect{ it.title } as Set<String>
        } else if(dataList) {
            //it could be a regular map in the case when the response was cached. in this case, get keys from the first row
            Map flatMap = MapFlattener.of(dataList[0]).convertEmptyStringsToNull(false).flatten()
            eb.includes = flatMap.keySet().toList()
        }

        Map pcfg = apiConfig.getPathMap(entityClass, null)
        Map colMap = columnLabels(pcfg)
        if (colMap) {
            //set the keys from the config
            eb.includes = colMap.keySet().toList()
            eb.headers = eb.includes.collect{
                String label = colMap[it]
                label ?: LabelUtils.getNaturalTitle(it)
            }
        }

        return eb
    }

    /**
     * looks for gridOptions.colModel on pathCofig and setup a map to the label
     */
    static Map<String, String> columnLabels(Map pathConfig){
        List colModel = PropertyTools.getProperty(pathConfig, 'gridOptions.colModel') as List<Map>
        Map colMap = [:] as Map<String, String>
        if(colModel){
            colModel.each {
                if(!it.hidden) {
                    //colModel would need to atleast specify field name, but if label is provided it would be used for xls header
                    //name is the name of domain field, label is what gets displayed as xls header
                    Validate.notEmpty(it.name, "name is required for columns in gridOptions.colMode")
                    colMap[(it.name as String)] = it.label as String
                }
            }
        }
        return colMap
    }

}
