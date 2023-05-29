/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepository
import gorm.tools.repository.model.LongIdGormRepo
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactSource

@GormRepository
@CompileStatic
class ContactSourceRepo extends LongIdGormRepo<ContactSource> {

    @Override
    ContactSource lookup(Map data) {
        if(data.sourceId) return ContactSource.findWhere(sourceId: data.sourceId)
        return null
    }

    Long findContactIdBySourceId(String sid) {
        List<Long> results = ContactSource.executeQuery('select contactId from ContactSource where sourceId = :sourceId',
            [sourceId: sid]) as List<Long>

        if(results) return results[0]
        else return null
    }

    ContactSource createSource(Contact c, Map data) {
        Map sourceData = [:]
        if(data.source && data.source instanceof Map){
            sourceData.putAll(data.source as Map)
        } else {
            ['sourceId', 'sourceType', 'source'].each {
                if(data[it]) sourceData[it] = data.remove(it)
            }
        }

        if(!sourceData.sourceId) {
            String sid = c.num
            if(!sid) sid = c.name
            if(!sid) sid = c.firstName
            sourceData.sourceId = sid
        }
        sourceData['contactId'] = c.id
        return ContactSource.create(sourceData)
    }
}
