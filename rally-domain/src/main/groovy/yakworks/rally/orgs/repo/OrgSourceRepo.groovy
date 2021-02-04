/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.IdGeneratorRepo
import grails.gorm.transactions.Transactional
import yakworks.rally.common.SourceType
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource

@GormRepository
@CompileStatic
class OrgSourceRepo implements GormRepo<OrgSource>, IdGeneratorRepo {

    @RepoListener
    void beforeValidate(OrgSource os) {
        generateId(os)
    }

    // @RepoListener
    // void afterBind(OrgSource orgSource, Map data, AfterBindEvent be){
    //     if (be.bindAction == BindAction.Create) {
    //         orgSource.sourceId = data['sourceId'] ?: data['num']
    //         orgSource.source = data['source'] ?: 'App'
    //         // orgSource.sourceType = data['sourceType'] ?: 'App'
    //     }
    // }

    /**
     * creates the source from org and its data and sets it to its org.source
     * @param org the org this is for
     * @param data the data used for the Org bind
     */
    OrgSource createSource(Org org, Map data) {
        Map sourceData = [:]
        //if data has source and its a data map then use it
        if(data.source && data.source instanceof Map){
            sourceData.putAll(data.source as Map)
        } else {
            //try pulling the keys off the data
            ['sourceId', 'sourceType', 'source'].each {
                if(data[it]) sourceData[it] = data.remove(it)
            }
        }
        if(!sourceData.sourceId) sourceData.sourceId = org.num
        sourceData.orgType = org.type
        sourceData.orgId = org.id
        sourceData.originator = true
        OrgSource os = new OrgSource()
        // skip create and just do the bind as we don't want to save it yet
        bind(os, sourceData, BindAction.Create)
        os.id = generateId()
        return os
    }

    //util method that creates the OrgSource from num and assigns to the source record (if originator)
    @Transactional
    OrgSource createSource(Org org, SourceType sourceType = SourceType.App, Boolean originator = true, String sourceDesc = '9ci') {
        OrgSource os = new OrgSource()
        os.orgId = org.id
        os.source = sourceDesc
        os.sourceType = sourceType
        os.orgType = org.type
        os.sourceId = org.num
        os.originator = originator
        os.persist() // will generate the id here as it manually assigned
        if (originator) org.source = os
        return os
    }

    /**
     * gets the SourceType from data so we can do it early and outside of the binder.
     * returns SourceType.App if sourceTypeObject is null
     */
    SourceType getSourceTypeFromData(Object sourceTypeObject) {
        if(!sourceTypeObject) return SourceType.App

        if (sourceTypeObject instanceof SourceType) {
            //assume its the enum
            return sourceTypeObject as SourceType
        } else if (sourceTypeObject instanceof String) {
            return SourceType.valueOf(sourceTypeObject as String) //must match exactly case sensitive
        }

    }
}
