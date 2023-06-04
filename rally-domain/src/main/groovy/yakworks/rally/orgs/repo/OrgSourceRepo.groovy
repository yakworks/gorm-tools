/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic

import gorm.tools.databinding.BindAction
import gorm.tools.mango.jpql.ComboKeyExistsQuery
import gorm.tools.model.SourceType
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.LongIdGormRepo
import grails.gorm.transactions.Transactional
import yakworks.api.problem.data.DataProblemCodes
import yakworks.commons.lang.Validate
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType

@GormRepository
@CompileStatic
class OrgSourceRepo extends LongIdGormRepo<OrgSource> {

    ComboKeyExistsQuery orgSourcExistsQuery

    @RepoListener
    void beforePersist(OrgSource os, BeforePersistEvent e) {
        if(os.isNew()) {
            //we check when new to avoid unique index error.
            if(exists(os.sourceType, os.sourceId, os.orgType)){
                throw DataProblemCodes.UniqueConstraint.get()
                    .detail("Violates unique constraint [sourceType: ${os.sourceType}, sourceId: ${os.sourceId}, orgType:${os.orgType}]")
                    .toException()
            }
        }
    }

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

    OrgSource findBySourceIdAndOrgType(String theSourceId, OrgType theOrgType) {
        return OrgSource.find('from OrgSource where sourceId = :sourceId and orgType = :orgType ',
            [sourceId: theSourceId, orgType: theOrgType])

        // return OrgSource.where { sourceId == theSourceId && orgType == theOrgType }.get() //back to find
    }

    List<Long> findOrgIdBySourceId(String theSourceId) {
        OrgSource.executeQuery('select orgId from OrgSource where sourceId = :sourceId',
            [sourceId: theSourceId] ) as List<Long>
    }

    Long findOrgIdBySourceIdAndOrgType(String theSourceId, OrgType theOrgType) {
        Validate.notNull(theOrgType, '[theOrgType]')
        Validate.notNull(theSourceId, '[theSourceId]')
        List res = OrgSource.executeQuery('select orgId from OrgSource where sourceId = :sourceId and orgType = :orgType',
            [sourceId: theSourceId, orgType: theOrgType] )
        // will only return 1
        res ? res[0] as Long : null
    }

    boolean exists(SourceType sourceType, String sourceId, OrgType orgType) {
        if( !orgSourcExistsQuery ) orgSourcExistsQuery = ComboKeyExistsQuery.of(getEntityClass())
            .keyNames(['sourceType','sourceId','orgType'])
        return orgSourcExistsQuery.exists(sourceType: sourceType, sourceId: sourceId, orgType: orgType)
    }

}
