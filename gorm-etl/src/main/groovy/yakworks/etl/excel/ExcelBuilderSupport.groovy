/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.etl.excel


import groovy.transform.CompileStatic

import yakworks.commons.beans.PropertyTools
import yakworks.commons.lang.LabelUtils
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
    static ExcelBuilder useIncludesConfig(ExcelBuilder eb, ApiConfig apiConfig, MetaMapList dataList){
        if(dataList.metaEntity){
            //flatten to get the titles
            Map<String, MetaProp> metaProps = dataList.metaEntity.flatten()
            eb.includes = metaProps.keySet().toList()
            Map pcfg = apiConfig.getPathMap(dataList.metaEntity.className, null)
            Map colMap = columnLabels(pcfg)
            if (colMap) {
                //set the keys from the config
                eb.includes = colMap.keySet().toList()
                //update to title to whats in col config.
                metaProps.each { String key, MetaProp mp ->
                    String label = colMap[key]
                    if (label) mp.title = label
                }
                eb.headers = eb.includes.collect{
                    MetaProp mp = metaProps[it]
                    mp ? mp.title : LabelUtils.getNaturalTitle(it)
                }
            }
            // eb.headerTitles = metaProps.values().collect{ it.title } as Set<String>
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
                    colMap[(it.name as String)] = it.label as String
                }
            }
        }
        return colMap
    }

}
