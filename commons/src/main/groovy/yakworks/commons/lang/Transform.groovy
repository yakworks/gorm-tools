/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import groovy.transform.CompileStatic

/**
 * helpers to transform one type to another
 *
 * @author Joshua Burnett (@basejump)
 */
@CompileStatic
class Transform {

    /**
     * simple util that collects a list into a Long list
     */
    static List<Long> toLongList(List dataList){
        if(!dataList) return []
        return dataList.collect { it as Long }
    }

    /**
     * simple util that collects a list of objects or maps as Long list,
     * can pass in idPropName for the field to collect
     */
    static List<Long> objectToLongList(List dataList, String idPropName = 'id'){
        if(!dataList) return []
        return dataList.collect { it[idPropName] as Long }
    }

    /**
     * converts a list of long ids to a list of maps with id key
     * so [1,2,3] would be converted to [[id:1], [id:2], [id:3]]
     * can also pass in a different key with idPropName
     */
    static List<Map> listToIdMap(List<Long> idList, String idPropName = 'id'){
        idList.collect { [(idPropName): it] } as List<Map>
    }

    /**
     * converts a list of objects with id to a list of maps with id key
     * so [1,2,3] would be converted to [[id:1], [id:2], [id:3]]
     * can also pass in a different key with idPropName
     */
    static List<Map> objectListToIdMapList(List entityList){
        entityList.collect { [id: it['id']] } as List<Map>
    }

}
